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
import com.example.periodvibe.data.local.AppDatabase
import com.example.periodvibe.data.repository.CycleRepository
import com.example.periodvibe.data.repository.SecurityRepository
import com.example.periodvibe.data.repository.SettingsRepository
import com.example.periodvibe.domain.model.Cycle
import com.example.periodvibe.domain.model.DailyRecord
import com.example.periodvibe.utils.AlarmScheduler
import com.example.periodvibe.utils.NotificationScheduler
import androidx.room.withTransaction
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
    private val database: AppDatabase,
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

    private val _showDisableAppLockDialog = MutableStateFlow(false)
    val showDisableAppLockDialog: StateFlow<Boolean> = _showDisableAppLockDialog.asStateFlow()

    private val _showTimeDialog = MutableStateFlow(false)
    val showTimeDialog: StateFlow<Boolean> = _showTimeDialog.asStateFlow()

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

    // 错误消息资源 ID，用于 Snackbar 反馈
    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    // 导入/导出/清除数据等耗时操作进行中（用于悬浮进度指示）
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // 当前选择的导出格式
    private var selectedExportFormat: ExportFormat = ExportFormat.JSON

    // 临时存储待导入的数据
    private var pendingImportData: Pair<List<Cycle>, List<DailyRecord>>? = null

    // 导入预览时产生的警告（如部分 CSV 行解析失败被跳过）
    private var pendingImportWarnings: List<String> = emptyList()

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
                        appLockDelayMinutes = settings.appLockDelayMinutes,
                        privacyModeEnabled = settings.privacyModeEnabled,
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
                        appLockDelayMinutes = 0,
                        privacyModeEnabled = false,
                        periodNotificationEnabled = true,
                        ovulationNotificationEnabled = true,
                        ovulationNotificationDaysBefore = 1
                    )
                    settingsRepository.insertSettings(defaultSettings)
                }
            }
        }
    }

    fun showDisableAppLockDialog() {
        _showDisableAppLockDialog.value = true
    }

    fun hideDisableAppLockDialog() {
        _showDisableAppLockDialog.value = false
    }

    fun showTimeDialog() {
        _showTimeDialog.value = true
    }

    fun hideTimeDialog() {
        _showTimeDialog.value = false
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
        pendingImportWarnings = emptyList()
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
            _isProcessing.value = true
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
                    _exportResult.value = Pair(true, context.getString(com.example.periodvibe.R.string.set_export_success, cycles.size, dailyRecords.size, selectedExportFormat.displayName))
                } else {
                    _exportResult.value = Pair(false, context.getString(com.example.periodvibe.R.string.set_write_file_failed))
                }
            } catch (e: Exception) {
                _exportResult.value = Pair(false, context.getString(com.example.periodvibe.R.string.set_export_failed_with_reason, e.message))
            } finally {
                _isProcessing.value = false
            }
            showExportResultDialog()
        }
    }

    /**
     * 从 Uri 预览导入数据（不实际导入）
     */
    fun previewImportData(uri: Uri) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                // 获取文件扩展名
                val fileExtension = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()

                // 读取文件全部内容
                val fileContent = csvExportImportService.readFileContent(context, uri)
                    ?: run {
                        _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_read_file_failed))
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
                                    pendingImportWarnings = result.warnings
                                    if (showNoDataErrorIfEmpty()) return@launch
                                    showImportConfirmationDialog()
                                    return@launch
                                }
                                is ImportResult.Failure -> {
                                    // JSON 失败，如果是 CSV 扩展名则尝试 CSV
                                    if (isCsvByExtension || fileType != CsvExportImportService.FileType.UNKNOWN) {
                                        // 继续尝试 CSV
                                    } else {
                                        _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_json_parse_failed, result.errorMessage))
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
                            fileType == CsvExportImportService.FileType.DAILY_RECORDS_CSV ||
                            isCsvByExtension -> {
                        try {
                            val (cyclesCsv, recordsCsv) = csvExportImportService.readCsvFromFile(context, uri)

                            if (cyclesCsv != null) {
                                val cyclesResult = csvExportImportService.importCyclesFromCsv(cyclesCsv)
                                when (cyclesResult) {
                                    is CsvImportResult.Success -> {
                                        // 给周期赋临时唯一 ID，确保日常记录能关联到正确的周期
                                        val cycles = cyclesResult.data.mapIndexed { index, cycle ->
                                            cycle.copy(id = index + 1L)
                                        }
                                        val (records, warnings) = if (recordsCsv != null) {
                                            when (val recordsResult = csvExportImportService.importDailyRecordsFromCsv(recordsCsv, cycles)) {
                                                is CsvImportResult.Success ->
                                                    recordsResult.data to (cyclesResult.warnings + recordsResult.warnings)
                                                is CsvImportResult.Failure ->
                                                    emptyList<DailyRecord>() to cyclesResult.warnings
                                            }
                                        } else {
                                            emptyList<DailyRecord>() to cyclesResult.warnings
                                        }
                                        pendingImportData = Pair(cycles, records)
                                        pendingImportWarnings = warnings
                                        if (showNoDataErrorIfEmpty()) return@launch
                                        showImportConfirmationDialog()
                                        return@launch
                                    }
                                    is CsvImportResult.Failure -> {
                                        _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_csv_parse_failed, cyclesResult.errorMessage))
                                        showImportResultDialog()
                                        return@launch
                                    }
                                }
                            } else if (recordsCsv != null) {
                                // 只有日常记录数据的 CSV
                                when (val recordsResult = csvExportImportService.importDailyRecordsFromCsv(recordsCsv, emptyList())) {
                                    is CsvImportResult.Success -> {
                                        pendingImportData = Pair(emptyList(), recordsResult.data)
                                        pendingImportWarnings = recordsResult.warnings
                                        if (showNoDataErrorIfEmpty()) return@launch
                                        showImportConfirmationDialog()
                                        return@launch
                                    }
                                    is CsvImportResult.Failure -> {
                                        _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_csv_parse_failed, recordsResult.errorMessage))
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
                    context.getString(
                        com.example.periodvibe.R.string.set_unrecognized_format,
                        fileExtension ?: context.getString(com.example.periodvibe.R.string.set_unknown),
                        fileContent.length
                    )
                )
                showImportResultDialog()
            } catch (e: Exception) {
                _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_import_failed_with_reason, e.javaClass.simpleName, e.message))
                showImportResultDialog()
            } finally {
                _isProcessing.value = false
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
     * 当待导入数据为空时显示错误并返回 true（防止覆盖模式清空现有数据）
     */
    private fun showNoDataErrorIfEmpty(): Boolean {
        val (cycles, records) = pendingImportData ?: return false
        if (cycles.isEmpty() && records.isEmpty()) {
            _importResult.value = ImportResult.Failure(
                context.getString(com.example.periodvibe.R.string.set_no_data_to_import)
            )
            showImportResultDialog()
            return true
        }
        return false
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
            _isProcessing.value = true
            try {
                val (cycles, dailyRecords) = pendingImportData ?: run {
                    _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_no_pending_import))
                    showImportResultDialog()
                    return@launch
                }

                if (cycles.isEmpty() && dailyRecords.isEmpty()) {
                    _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_no_data_to_import))
                    hideImportConfirmationDialog()
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
                _importResult.value = ImportResult.Failure(context.getString(com.example.periodvibe.R.string.set_import_failed_simple, e.message))
                hideImportConfirmationDialog()
                showImportResultDialog()
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * 覆盖模式导入
     */
    private suspend fun importOverwrite(cycles: List<Cycle>, dailyRecords: List<DailyRecord>) {
        // 整个"清空+写入"在单事务中执行，中途失败自动回滚，避免旧数据全部丢失
        database.withTransaction {
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
        }

        // 重新安排通知
        tryRescheduleNotification()

        // 结果仅用于展示数量统计
        _importResult.value = ImportResult.Success(cycles, dailyRecords, warnings = pendingImportWarnings)
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

        val newCyclesWithIds = database.withTransaction {
            // 插入新周期
            val insertedCycleIds = cycleRepository.insertAllCycles(newCycles)
            val withIds = newCycles.zip(insertedCycleIds) { cycle, newId ->
                cycle.copy(id = newId)
            }

            // 构建完整的 startDate -> cycleId 映射（包含旧的和新的）
            val allCycles = existingCycles + withIds
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
            Pair(withIds, newRecords)
        }

        // 重新安排通知
        tryRescheduleNotification()

        _importResult.value = ImportResult.Success(
            newCyclesWithIds.first,
            newCyclesWithIds.second,
            warnings = pendingImportWarnings
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

    fun rescheduleNotifications() {
        viewModelScope.launch {
            tryRescheduleNotification()
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

    fun updateAppLockDelay(minutes: Int) {
        viewModelScope.launch {
            val currentSettings = settingsRepository.getSettingsSync()
            currentSettings?.let {
                val updatedSettings = it.copy(appLockDelayMinutes = minutes)
                settingsRepository.updateSettings(updatedSettings)
            }
        }
    }

    /** 验证当前 PIN（关闭应用锁等场景），失败计入递增锁定计数 */
    fun verifyCurrentPin(pin: String): Boolean {
        val matches = securityRepository.getPin() == pin
        if (!matches) {
            securityRepository.recordFailedAttempt()
        } else {
            securityRepository.resetFailedAttempts()
        }
        return matches
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
            _isProcessing.value = true
            try {
                // 删除所有周期和日常记录数据
                cycleRepository.deleteAllDailyRecords()
                cycleRepository.deleteAllCycles()
                // 取消所有通知
                alarmScheduler.cancelAll()
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = com.example.periodvibe.R.string.error_clear_data_failed
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun consumeError() {
        _errorMessage.value = null
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
        val appLockDelayMinutes: Int,
        val privacyModeEnabled: Boolean,
        val periodNotificationEnabled: Boolean,
        val ovulationNotificationEnabled: Boolean,
        val ovulationNotificationDaysBefore: Int
    ) : SettingsUiState()
}
