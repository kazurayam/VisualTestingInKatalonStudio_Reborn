import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.ks.globalvariable.ExecutionProfilesLoader
import com.kazurayam.materialstore.MaterialstoreFacade
import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kazurayam.materialstore.metadata.QueryOnMetadata
import com.kazurayam.materialstore.resolvent.ArtifactGroup
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import internal.GlobalVariable

/**
 * Flaskr/VisualInspection_Twins
 *
 */

Path projectDir = Paths.get(RunConfiguration.getProjectDir())
Path root = projectDir.resolve("store")
Store store = Stores.newInstance(root)
JobName jobName = new JobName("Flaskr_VisualInspectionTwins")

ExecutionProfilesLoader profilesLoader = new ExecutionProfilesLoader()


//---------------------------------------------------------------------
/*
 * Materialize stage
 */
// visit the Production environment
String profile1 = "Flaskr_ProductionEnv"
profilesLoader.loadProfile(profile1)
WebUI.comment("Execution Profile ${profile1} was loaded with GlobalVariable.URL=${GlobalVariable.URL}")
JobTimestamp timestampP = JobTimestamp.now()

WebUI.callTestCase(
	findTestCase("main/Flaskr/materialize"),
	["profile": profile1, "store": store, "jobName": jobName, "jobTimestamp": timestampP]
)

// visit the Development environment
String profile2 = "Flaskr_DevelopmentEnv"
profilesLoader.loadProfile(profile2)
WebUI.comment("Execution Profile ${profile2} was loaded with GlobalVariable.URL=${GlobalVariable.URL}")
JobTimestamp timestampD = JobTimestamp.laterThan(timestampP)

WebUI.callTestCase(
	findTestCase("main/Flaskr/materialize"),
	["profile": profile2, "store": store, "jobName": jobName, "jobTimestamp": timestampD]
)



// --------------------------------------------------------------------
/*
 * Reduce stage
 */
// compare the materials obtained from the 2 sites, compile a diff report
// pickup the materials that belongs to the 2 "profiles"
MaterialList left = store.select(jobName, timestampP,
	QueryOnMetadata.builderWithMap([ "profile": profile1 ]).build()
	)

MaterialList right = store.select(jobName, timestampD,
	QueryOnMetadata.builderWithMap([ "profile": profile2 ]).build()
	)
	
// zip 2 Materials to form a single Artifact
ArtifactGroup prepared =
	ArtifactGroup.builder(left, right)
		.ignoreKeys("profile", "URL.host", "URL.port")
		.sort("step")
		.build()

// with 2 Materials, create a diff Material
MaterialstoreFacade facade = MaterialstoreFacade.newInstance(store)	
ArtifactGroup reduced = facade.reduce(prepared)



//---------------------------------------------------------------------
/*
 * Report stage
 */
// if difference is greater than this critera, it should be warned
double criteria = 0.0d
	
// the file name of HTML report
String fileName = jobName.toString() + "-index.html"
	
Path report = facade.report(jobName, reduced, criteria, fileName)
	
assert Files.exists(report)
WebUI.comment("The report can be found ${report.toString()}")

// if any significant difference found, then this Test Case should warn it
int warnings = reduced.countWarnings(criteria)
if (warnings > 0) {
	KeywordUtil.markWarning("found ${warnings} differences.")
}
	