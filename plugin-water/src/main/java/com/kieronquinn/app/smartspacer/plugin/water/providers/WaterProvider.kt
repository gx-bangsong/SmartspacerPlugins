import android.content.ComponentName
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.plugin.water.R
import com.kieronquinn.app.smartspacer.plugin.water.repositories.DisplayMode
import com.kieronquinn.app.smartspacer.plugin.water.repositories.WaterDataRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate

class WaterProvider : SmartspacerTargetProvider(), KoinComponent {

    private val waterDataRepository by inject<WaterDataRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val today = LocalDate.now()
        val schedule = waterDataRepository.getDailySchedule(today) ?: return emptyList()

        val text = when (waterDataRepository.displayMode) {
            DisplayMode.PROGRESS -> "Water: ${schedule.fulfilledCount} / ${schedule.cupsTotal} cups"
            DisplayMode.REMINDER -> "Time for a glass of water!"
            DisplayMode.DYNAMIC -> {
                val nextReminder = schedule.scheduledTimes.getOrNull(schedule.fulfilledCount)
                if (nextReminder != null && nextReminder <= System.currentTimeMillis()) {
                    "Time for a glass of water!"
                } else {
                    "Water: ${schedule.fulfilledCount} / ${schedule.cupsTotal} cups"
                }
            }
        }

        val target = SmartspaceTarget.UI( // may be .Ui depending on version
            id = "water_progress",
            componentName = ComponentName(requireNotNull(context), javaClass),
            header = Text("Water Progress"), // sometimes named 'title'
            primaryText = Text(text),
            icon = Icon(android.graphics.drawable.Icon.createWithResource(requireNotNull(context), R.drawable.ic_launcher_foreground)),
            tapAction = TapAction(
                intent = requireNotNull(context).packageManager.getLaunchIntentForPackage(requireNotNull(context).packageName)
            )
        )
        return listOf(target)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            "Water Reminder",
            "Track your water intake",
            android.graphics.drawable.Icon.createWithResource(requireNotNull(context), R.drawable.ic_launcher_foreground),
            configActivity = Intent(requireNotNull(context), com.kieronquinn.app.smartspacer.plugin.water.ui.activities.SettingsActivity::class.java)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false
    }
}
