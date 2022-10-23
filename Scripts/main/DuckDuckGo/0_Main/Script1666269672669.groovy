import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.materialstore.filesystem.JobName
import com.kazurayam.materialstore.filesystem.JobTimestamp
import com.kazurayam.materialstore.filesystem.MaterialList
import com.kazurayam.materialstore.filesystem.Store
import com.kazurayam.materialstore.filesystem.Stores
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI


/**
 * Test Cases/main/DuckDuckGo/Main
 *
 */
Path projectDir = Paths.get(RunConfiguration.getProjectDir())
Path root = projectDir.resolve("store")
Store store = Stores.newInstance(root)
JobName jobName = new JobName("DuckDuckGo")
JobTimestamp jobTimestamp = JobTimestamp.now()



//---------------------------------------------------------------------
/*
 * materialize stage
 */
MaterialList materialList = WebUI.callTestCase(
	findTestCase("main/DuckDuckGo/2_materialize"),
	["store": store, "jobName": jobName, "jobTimestamp": jobTimestamp])



//---------------------------------------------------------------------
/*
 * report stage
 */
Path report =
	WebUI.callTestCase(findTestCase("main/DuckDuckGo/4_report"),
		["store": store, "materialList": materialList])


//---------------------------------------------------------------------
/*
 * cleanup
 */
int deletedStuff =
	WebUI.callTestCase(findTestCase("main/DuckDuckGo/6_cleanup"),
		["store": store, "jobName": jobName])
	

//---------------------------------------------------------------------
/*
 * create store/index.html	
 */
Path index =
    WebUI.callTestCase(findTestCase("main/DuckDuckGo/7_index"),
		["store": store])

	
	
//---------------------------------------------------------------------
/*
 * Epilogue
 */
WebUI.comment("see ${report.toString()} for the list")
