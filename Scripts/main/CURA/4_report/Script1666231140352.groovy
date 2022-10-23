import java.nio.file.Files
import java.nio.file.Path

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.inspector.Inspector
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

/**
 * Test Cases/main/CURA/4_report
 */
assert store != null
assert mProductGroup != null
assert mProductGroup.isReadyToReport()
assert sortKeys != null
assert criteria != null

JobName jobName = mProductGroup.getJobName()
JobTimestamp jobTimestamp = mProductGroup.getJobTimestampRight()

WebUI.comment("report started; store=${store}")
println "mProductGroup=${mProductGroup.toSummary()}" 

Inspector inspector = Inspector.newInstance(store)
inspector.setSortKeys(sortKeys)
Path report = inspector.report(mProductGroup, criteria)

assert Files.exists(report)
WebUI.comment("The report can be found at ${report.toString()}")

int warnings = mProductGroup.countWarnings(criteria)
return warnings