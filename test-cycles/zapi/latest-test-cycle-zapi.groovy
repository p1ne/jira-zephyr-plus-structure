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

if (issue.getIssueType().name.equals("Test")) {
	return ""
}

// Installation-specific
def baseURLPrefix = "http://jira";

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
while ((inputLine = reader.readLine()) != null) {
  	response.append(inputLine)
}
reader.close();

def slurper = new JsonSlurper()
def result = slurper.parseText(response.toString())

if (result.recordsCount.toInteger() > 0) {
   return result.executions[0].versionName;
} else {
	return ""
}
