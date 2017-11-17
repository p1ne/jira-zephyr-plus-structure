import groovy.json.JsonSlurper;
import groovy.json.StreamingJsonBuilder;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue
import org.apache.commons.codec.binary.Base64;

if (!issue.getIssueType().name.equals("Test")) {
	return ""
}

// Installation-specific
def baseURLPrefix = "http://jira";

enableCache = {-> false};

String issueType = issue.getIssueType().name;

def issueID = "";
IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
issueID = issue.getId();

def baseURL = baseURLPrefix + "/rest/zapi/latest/execution?issueId=" + issueID;

URL url;

url = new URL(baseURL);

URLConnection urlConnection = url.openConnection();
urlConnection.setRequestMethod("GET");
BufferedReader reader = new BufferedReader(
	new InputStreamReader(urlConnection.getInputStream()));
StringBuffer response = new StringBuffer();
String inputLine;

while ((inputLine = reader.readLine()) != null)
{
	response.append(inputLine)
}
reader.close();

def slurper = new JsonSlurper()
def result = slurper.parseText(response.toString())

def statusName = result.status[result.executions[0].executionStatus].name
def versionName = result.executions[0].versionName

def retString = ""

if (versionName.equals("Unscheduled")) {
	retString = "<a id=\"zephyr-je-add-execute\" title=\"Execute Test\" class=\"toolbar-trigger viewissue-add-execute\" href=\"/secure/AddExecute!AddExecute.jspa?id=" + issueID + "\"><span class=\"trigger-label\">Execute</span></a>";
} else if (!versionName.equals("Unscheduled") && (statusName.equals("UNEXECUTED") || statusName.equals("IN PROGRESS")) ) {
	retString = "<a href=\"/secure/enav/#/" + result.executions[0].id.toString() + "\"><span class=\"trigger-label\">Execute</span></a>";
} else {
	retString = "<a id=\"zephyr-je-add-execute\" title=\"Execute Test\" class=\"toolbar-trigger viewissue-add-execute\" href=\"/secure/AddExecute!AddExecute.jspa?id=" + issueID + "\"><span class=\"trigger-label\">Execute</span></a>";
}

return(retString)
