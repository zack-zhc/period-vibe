package com.example.periodvibe.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.periodvibe.data.exportimport.CsvExportImportService
import com.example.periodvibe.data.exportimport.CsvImportResult
import com.example.periodvibe.data.exportimport.DataExportImportService
import com.example.periodvibe.data.exportimport.ExportFormat
import com.example.periodvibe.data.exportimport.ImportResult
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.data.repository.SecurityRepository
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.utils.AlarmScheduler
import com.example.periodvibe.utils.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val cycleRepository: CycleRepository,
    private val securityRepository: SecurityRepository,
    private val dataExportImportService: DataExportImportService,
    private val csvExportImportService: CsvExportImportService,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val alarmScheduler = AlarmScheduler(context)

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _showCycleDialog = MutableStateFlow(false)
    val showCycleDialog: StateFlow<Boolean> = _showCycleDialog.asStateFlow()

    private val _showDisableAppLockDialog = MutableStateFlow(false)
    val showDisableAppLockDialog: StateFlow<Boolean> = _showDisableAppLockDialog.asStateFlow()

    private val _showNotificationDialog = MutableStateFlow(false)
    val showNotificationDialog: StateFlow<Boolean> = _showNotificationDialog.asStateFlow()

    private val _showDaysBeforeDialog = MutableStateFlow(false)
    val showDaysBeforeDialog: StateFlow<Boolean> = _showDaysBeforeDialog.asStateFlow()

    private val _showTimeDialog = MutableStateFlow(false)
    val showTimeDialog: StateFlow<Boolean> = _showTimeDialog.asStateFlow()

    private val _showOvulationDaysDialog = MutableStateFlow(false)
    val showOvulationDaysDialog: StateFlow<Boolean> = _showOvulationDaysDialog.asStateFlow()

    private val _showThemeDialog = MutableStateFlow(false)
    val showThemeDialog: StateFlow<Boolean> = _showThemeDialog.asStateFlow()

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog: StateFlow<Boolean> = _showAboutDialog.asStateFlow()

    private val _showClearDataConfirmationDialog = MutableStateFlow(false)
    val showClearDataConfirmationDialog: StateFlow<Boolean> = _showClearDataConfirmationDialog.asStateFlow()

    private val _showImportConfirmationDialog = MutableStateFlow(false)
    val showImportConfirmationDialog: StateFlow<Boolean> = _showImportConfirmationDialog.asStateFlow()

    private val _showImportResultDialog = MutableStateFlow(false)
    val showImportResultDialog: StateFlow<Boolean> = _showImportResultDialog.asStateFlow()

    private val _showExportResultDialog = MutableStateFlow(false)
    val showExportResultDialog: StateFlow<Boolean> = _showExportResultDialog.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _exportResult = MutableStateFlow<Pair<Boolean, String>?>(null)
    val exportResult: StateFlow<Pair<Boolean, String>?> = _exportResult.asStateFlow()

    private val _showExportFormatDialog = MutableStateFlow(false)
    val showExportFormatDialog: StateFlow<Boolean> = _showExportFormatDialog.asStateFlow()

    // 当前选择的导出格式
    private var selectedExportFormat: ExportFormat = ExportFormat.JSON

    // 临时存储待导入的数据
    private var pendingImportData: Pair<List<Cycle>, List<DailyRecord>>? = null

    // 导入模式
    enum class ImportMode {
        MERGE,  // 合并模式：跳过已存在的日期
        OVERWRITE  // 覆盖模式：删除所有现有数据后导入
    }

    private var pendingImportMode: ImportMode = ImportMode.OVERWRITE

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                if (settings != null) {
                    _uiState.value = SettingsUiState.Success(
                        autoCalculateCycle = settings.autoCalculateCycle,
                        cycleLengthDefault = settings.cycleLengthDefault,
                        periodLengthDefault = settings.periodLengthDefault,
                        cycleLengthRange = settings.cycleLengthRange,
                        periodLengthRange = settings.periodLengthRange,
                        notificationEnabled = settings.notificationEnabled,
                        notificationDaysBefore = settings.notificationDaysBefore,
                        notificationTime = settings.notificationTime,
                        themeMode = settings.themeMode,
                        appLockEnabled = settings.appLockEnabled,
                        privacyModeEnabled = settings.privacyModeEnabled,
                        language = settings.language,
                        periodNotificationEnabled = settings.periodNotificationEnabled,
                        ovulationNotificationEnabled = settings.ovulationNotificationEnabled,
                        ovulationNotificationDaysBefore = settings.ovulationNotificationDaysBefore
                    )
                } else {
                    // 创建默认设置
                    val defaultSettings = com.example.periodvibe.domain.model.Settings(
                        autoCalculateCycle = true,
                        cycleLengthDefault = 28,
                        periodLengthDefault = 5,
                        cycleLengthRange = 21..35,
                        periodLengthRange = 3..7,
                        notificationEnabled = true,
                        notificationDaysBefore = 1,
                        notificationTime = java.time.LocalTime.of(9, 0),
                        themeMode = com.example.periodvibe.domain.model.Settings.ThemeMode.SYSTEM,
                        appLockEnabled = false,
                        privacyModeEnabled = false,
                        language = "zh",
                        periodNotificationEnabled = true,
                        ovulationNotificationEnabled = true,
                        ovulationNotificationDaysBefore = 1
                    )
                    settingsRepository.insertSettings(defaultSettings)
                }
            }
        }
    }

    fun showCycleDialog() {
        _showCycleDialog.value = true
    }

    fun hideCycleDialog() {
        _showCycleDialog.value = false
    }

    fun showDisableAppLockDialog() {
        _showDisableAppLockDialog.value = true
    }

    fun hideDisableAppLockDialog() {
        _showDisableAppLockDialog.value = false
    }

    fun showNotificationDialog() {
        _showNotificationDialog.value = true
    }

    fun hideNotificationDialog() {
        _showNotificationDialog.value = false
    }

    fun showDaysBeforeDialog() {
        _showDaysBeforeDialog.value = true
    }

    fun hideDaysBeforeDialog() {
        _showDaysBeforeDialog.value = false
    }

    fun showTimeDialog() {
        _showTimeDialog.value = true
    }

    fun hideTimeDialog() {
        _showTimeDialog.value = false
    }

    fun showOvulationDaysDialog() {
        _showOvulationDaysDialog.value = true
    }

    fun hideOvulationDaysDialog() {
        _showOvulationDaysDialog.value = false
    }

    fun showThemeDialog() {
        _showThemeDialog.value = true
    }

    fun hideThemeDialog() {
        _showThemeDialog.value = false
    }

    fun showAboutDialog() {
        _showAboutDialog.value = true
    }

    fun hideAboutDialog() {
        _showAboutDialog.value = false
    }

    fun showClearDataConfirmationDialog() {
        _showClearDataConfirmationDialog.value = true
    }

    fun hideClearDataConfirmationDialog() {
        _showClearDataConfirmationDialog.value = false
    }

    fun showImportConfirmationDialog() {
        _showImportConfirmationDialog.value = true
    }

    fun hideImportConfirmationDialog() {
        _showImportConfirmationDialog.value = false
        pendingImportData = null
    }

    fun showImportResultDialog() {
        _showImportResultDialog.value = true
    }

    fun hideImportResultDialog() {
        _showImportResultDialog.value = false
        _importResult.value = null
    }

    fun showExportResultDialog() {
        _showExportResultDialog.value = true
    }

    fun hideExportResultDialog() {
        _showExportResultDialog.value = false
        _exportResult.value = null
    }

    fun showExportFormatDialog() {
        _showExportFormatDialog.value = true
    }

    fun hideExportFormatDialog() {
        _showExportFormatDialog.value = false
    }

    fun setSelectedExportFormat(format: ExportFormat) {
        selectedExportFormat = format
        hideExportFormatDialog()
    }

    fun getSelectedExportFormat(): ExportFormat = selectedExportFormat

    /**
     * 导出数据到 Uri
     */
    fun exportData(uri: Uri) {
        viewModelScope.launch {
            try {
                val cycles = cycleRepository.getAllCyclesOnce()
                val dailyRecords = cycleRepository.getAllDailyRecordsOnce()
                val success = when (selectedExportFormat) {
                    ExportFormat.JSON -> {
                        val jsonString = dataExportImportService.exportToJson(cycles, dailyRecords)
                        dataExportImportService.writeToFile(context, uri, jsonString)
                    }
                    ExportFormat.CSV -> {
                        val cyclesCsv = csvExportImportService.exportCyclesToCsv(cycles)
                        val recordsCsv = csvExportImportService.exportDailyRecordsToCsv(dailyRecords, cycles)
                        csvExportImportService.writeCsvToFile(context, uri, cyclesCsv, recordsCsv)
                    }
                }

                if (success) {
                    _exportResult.value = Pair(true, "成功导出 ${cycles.size} 个周期记录和 ${dailyRecords.size} 条日常记录 (${selectedExportFormat.displayName} 格式)")
                } else {
                    _exportResult.value = Pair(false, "写入文件失败")
                }
            } catch (e: Exception) {
                _exportResult.value = Pair(false, "导出失败: ${e.message}")
            }
            showExportResultDialog()
        }
    }

    /**
     * 从 Uri 预览导入数据（不实际导入）
     */
    fun previewImportData(uri: Uri) {
        viewModelScope.launch {
            try {
                // 获取文件扩展名
                val fileExtension = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()

                // 读取文件全部内容
                val fileContent = csvExportImportService.readFileContent(context, uri)
                    ?: run {
                        _importResult.value = ImportResult.Failure("无法读取文件")
                        showImportResultDialog()
                        return@launch
                    }

                // 检测文件类型 - 先通过扩展名猜测
                val isJsonByExtension = fileExtension == "json"
                val isCsvByExtension = fileExtension == "csv"

                // 通过内容检测
                val fileType = csvExportImportService.detectFileType(fileContent)

                // 根据检测结果尝试解析
                when {
                    // 先尝试 JSON（通过内容或扩展名判断）
                    fileType == CsvExportImportService.FileType.JSON || isJsonByExtension -> {
                        try {
                            val result = dataExportImportService.importFromJson(fileContent)
                            when (result) {
                                is ImportResult.Success -> {
                                    pendingImportData = Pair(result.cycles, result.dailyRecords)
                                    showImportConfirmationDialog()
                                    return@launch
                                }
                                is ImportResult.Failure -> {
                                    // JSON 失败，如果是 CSV 扩展名则尝试 CSV
                                    if (isCsvByExtension || fileType != CsvExportImportService.FileType.UNKNOWN) {
                                        // 继续尝试 CSV
                                    } else {
                                        _importResult.value = ImportResult.Failure("JSON 格式解析失败: ${result.errorMessage}")
                                        showImportResultDialog()
                                        return@launch
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // JSON 解析异常，继续尝试 CSV
                        }
                    }
                }

                // 尝试 CSV（通过内容或扩展名判断）
                when {
                    fileType == CsvExportImportService.FileType.COMBINED_CSV ||
                            fileType == CsvExportImportService.FileType.CYCLES_CSV ||
                            isCsvByExtension -> {
                        try {
                            val (cyclesCsv, recordsCsv) = csvExportImportService.readCsvFromFile(context, uri)

                            if (cyclesCsv != null) {
                                val cyclesResult = csvExportImportService.importCyclesFromCsv(cyclesCsv)
                                when (cyclesResult) {
                                    is CsvImportResult.Success -> {
                                        val cycles = cyclesResult.data
                                        val records = if (recordsCsv != null) {
                                            when (val recordsResult = csvExportImportService.importDailyRecordsFromCsv(recordsCsv, cycles)) {
                                                is CsvImportResult.Success -> recordsResult.data
                                                is CsvImportResult.Failure -> emptyList()
                                            }
                                        } else {
                                            emptyList()
                                        }
                                        pendingImportData = Pair(cycles, records)
                                        showImportConfirmationDialog()
                                        return@launch
                                    }
                                    is CsvImportResult.Failure -> {
                                        _importResult.value = ImportResult.Failure("CSV 格式解析失败: ${cyclesResult.errorMessage}")
                                        showImportResultDialog()
                                        return@launch
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // CSV 解析异常，继续
                        }
                    }
                }

                // 所有方法都失败了
                _importResult.value = ImportResult.Failure(
                    "无法识别文件格式\n" +
                            "请确保使用 JSON 或 CSV 格式的备份文件\n" +
                            "检测到的扩展名: ${fileExtension ?: "未知"}\n" +
                            "文件大小: ${fileContent.length} 字符"
                )
                showImportResultDialog()
            } catch (e: Exception) {
                _importResult.value = ImportResult.Failure("导入失败: ${e.javaClass.simpleName} - ${e.message}")
                showImportResultDialog()
            }
        }
    }

    /**
     * 获取待导入数据的数量信息
     */
    fun getPendingImportDataCount(): Pair<Int, Int> {
        return pendingImportData?.let { (cycles, records) ->
            Pair(cycles.size, records.size)
        } ?: Pair(0, 0)
    }

    /**
     * 设置导入模式
     */
    fun setImportMode(mode: ImportMode) {
        pendingImportMode = mode
    }

    /**
     * 确认导入数据
     */
    fun confirmImportData() {
        viewModelScope.launch {
            try {
                val (cycles, dailyRecords) = pendingImportData ?: run {
                    _importResult.value = ImportResult.Failure("没有待导入的数据")
                    showImportResultDialog()
                    return@launch
                }

                when (pendingImportMode) {
                    ImportMode.OVERWRITE -> {
                        importOverwrite(cycles, dailyRecords)
                    }
                    ImportMode.MERGE -> {
                        importMerge(cycles, dailyRecords)
                    }
                }
            } catch (e: Exception) {
                _importResult.value = ImportResult.Failure("导入失败: ${e.message}")
                hideImportConfirmationDialog()
                showImportResultDialog()
            }
        }
    }

    /**
     * 覆盖模式导入
     */
    private suspend fun importOverwrite(cycles: List<Cycle>, dailyRecords: List<DailyRecord>) {
        // 先清除现有数据
        cycleRepository.deleteAllDailyRecords()
        cycleRepository.deleteAllCycles()

        // 导入新数据 - 先插入周期，获取新生成的 ID
        val insertedCycleIds = cycleRepository.insertAllCycles(cycles)
        val cyclesWithIds = cycles.zip(insertedCycleIds) { cycle, newId ->
            cycle.copy(id = newId)
        }

        // 更新日常记录的 cycleId 为新生成的 ID
        val originalStartDateToNewId = cyclesWithIds.associate { it.startDate to it.id }
        val recordsWithUpdatedCycleIds = dailyRecords.map { record ->
            val originalCycle = cycles.find { cycle ->
                record.cycleId?.let { cycle.id == it } == true
            }
            val newCycleId = originalCycle?.startDate?.let { originalStartDateToNewId[it] }
            record.copy(cycleId = newCycleId)
        }

        // 插入日常记录
        cycleRepository.insertAllDailyRecords(recordsWithUpdatedCycleIds)

        // 重新安排通知
        tryRescheduleNotification()

        _importResult.value = ImportResult.Success(cyclesWithIds, recordsWithUpdatedCycleIds)
        hideImportConfirmationDialog()
        showImportResultDialog()
    }

    /**
     * 合并模式导入
     */
    private suspend fun importMerge(cycles: List<Cycle>, dailyRecords: List<DailyRecord>) {
        // 获取现有数据
        val existingCycles = cycleRepository.getAllCyclesOnce()
        val existingRecords = cycleRepository.getAllDailyRecordsOnce()

        // 现有数据的日期集合
        val existingCycleDates = existingCycles.map { it.startDate }.toSet()
        val existingRecordDates = existingRecords.map { it.date }.toSet()

        // 筛选出新的周期（startDate 不存在的）
        val newCycles = cycles.filter { it.startDate !in existingCycleDates }

        // 插入新周期
        val insertedCycleIds = cycleRepository.insertAllCycles(newCycles)
        val newCyclesWithIds = newCycles.zip(insertedCycleIds) { cycle, newId ->
            cycle.copy(id = newId)
        }

        // 构建完整的 startDate -> cycleId 映射（包含旧的和新的）
        val allCycles = existingCycles + newCyclesWithIds
        val startDateToCycleId = allCycles.associate { it.startDate to it.id }

        // 筛选出新的日常记录，同时更新它们的 cycleId
        val newRecords = dailyRecords
            .filter { it.date !in existingRecordDates }
            .map { record ->
                // 找到这条记录对应的原始周期
                val originalCycle = cycles.find { cycle ->
                    record.cycleId?.let { cycle.id == it } == true
                }
                // 通过原始周期的 startDate 查找新的 cycleId
                val newCycleId = originalCycle?.startDate?.let { startDateToCycleId[it] }
                record.copy(cycleId = newCycleId)
            }

        // 插入新的日常记录
        cycleRepository.insertAllDailyRecords(newRecords)

        // 重新安排通知
        tryRescheduleNotification()

        _importResult.value = ImportResult.Success(
            newCyclesWithIds,
            newRecords
        )
        hideImportConfirmationDialog()
        showImportResultDialog()
    }

    private suspend fun tryRescheduleNotification() {
        try {
            notificationScheduler.rescheduleAllNotifications()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateCycleParameters(
        cycleLength: Int,
        periodLength: Int
    ) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.updateCycleParameters(cycleLength, periodLength)
                settingsRepository.updateSettings(updatedSettings)
            }
            hideCycleDialog()
        }
    }

    fun updateCycleLength(cycleLength: Int) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(cycleLengthDefault = cycleLength)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun updatePeriodLength(periodLength: Int) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(periodLengthDefault = periodLength)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun toggleAutoCalculateCycle(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(autoCalculateCycle = enabled)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun updateNotificationSettings(
        enabled: Boolean,
        daysBefore: Int,
        time: LocalTime
    ) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.updateNotificationSettings(enabled, daysBefore, time)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
            hideNotificationDialog()
        }
    }

    fun updateThemeMode(mode: com.example.periodvibe.domain.model.Settings.ThemeMode) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.updateThemeMode(mode)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !securityRepository.hasPin()) {
                // Should navigate to PinSetupScreen, but ViewModel can't navigate.
                // This is handled in the UI layer (SettingsScreen)
            } else {
                val currentSettings = settingsRepository.getSettingsSync()
                currentSettings?.let {
                    val updatedSettings = it.copy(appLockEnabled = enabled)
                    settingsRepository.updateSettings(updatedSettings)
                }
            }
            hideDisableAppLockDialog()
        }
    }

    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(privacyModeEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
        }
    }

    fun toggleNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(notificationEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
        }
    }

    fun updateNotificationDaysBefore(daysBefore: Int) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(notificationDaysBefore = daysBefore)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
            hideDaysBeforeDialog()
        }
    }

    fun updateNotificationTime(time: LocalTime) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(notificationTime = time)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
            hideTimeDialog()
        }
    }

    fun togglePeriodNotification(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(periodNotificationEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
        }
    }

    fun toggleOvulationNotification(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(ovulationNotificationEnabled = enabled)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
        }
    }

    fun updateOvulationDaysBefore(daysBefore: Int) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(ovulationNotificationDaysBefore = daysBefore)
                settingsRepository.updateSettings(updatedSettings)
                notificationScheduler.rescheduleAllNotifications()
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            hideClearDataConfirmationDialog()
            // 删除所有周期和日常记录数据
            cycleRepository.deleteAllDailyRecords()
            cycleRepository.deleteAllCycles()
            // 取消所有通知
            alarmScheduler.cancelAll()
        }
    }
}

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(
        val autoCalculateCycle: Boolean,
        val cycleLengthDefault: Int,
        val periodLengthDefault: Int,
        val cycleLengthRange: IntRange,
        val periodLengthRange: IntRange,
        val notificationEnabled: Boolean,
        val notificationDaysBefore: Int,
        val notificationTime: LocalTime,
        val themeMode: com.example.periodvibe.domain.model.Settings.ThemeMode,
        val appLockEnabled: Boolean,
        val privacyModeEnabled: Boolean,
        val language: String,
        val periodNotificationEnabled: Boolean,
        val ovulationNotificationEnabled: Boolean,
        val ovulationNotificationDaysBefore: Int
    ) : SettingsUiState()
}
