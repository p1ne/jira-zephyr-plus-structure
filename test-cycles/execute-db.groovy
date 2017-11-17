import com.atlassian.jira.component.ComponentAccessor
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface

import java.sql.Connection

enableCache = {-> false}

if (!issue.getIssueType().name.equals("Test")) {
	return ""
}

// Installation-specific
def status = [
	'':		['UNEXECUTED', '#A0A0A0'],
	'-1':	['UNEXECUTED', '#A0A0A0'],
	'1':	['PASS', '#75B000'],
	'2':	['FAIL', '#CC3300'],
	'3':	['IN PROGRESS', '#F2B000'],
	'4':	['BLOCKED', '#cc33ff'],
	'5':	['NOT READY TO TEST', '#cc33ff'],
	'6':	['POSTPONED', '#00ff00'],
	'7':	['INTERNAL TEST', '#ffcc00'],
	'8':	['READY TO TEST', '#3333ff'],
	'9':	['DRAFT', '#000000']
]

def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
String helperName = delegator.getGroupHelperName("default");
StringBuffer sb1 = new StringBuffer()
StringBuffer sb2 = new StringBuffer()
StringBuffer sb3 = new StringBuffer()

String projectId = issue.getProjectId().toString()
String issueId = issue.getId().toString()

def sqlStmt = "SELECT schedule.status, version.vname, schedule.id FROM ao_7deabf_schedule schedule, projectversion version where schedule.project_id=" + projectId + " and schedule.issue_id=" + issueId + " and schedule.version_id = version.id order by schedule.date_created desc limit 1";

Connection conn = ConnectionFactory.getConnection(helperName);
Sql sql = new Sql(conn)

try {
  sql.eachRow(sqlStmt) {
	  sb1 << "${it.status}"
	  sb2 << "${it.vname}"
	  sb3 << "${it.id}"
  }
} finally {
    sql.close()
}

def statusName =  status[sb1.toString()][0]
def versionName = sb2.toString()
def executionId = sb3.toString()

def retString = ""

if (versionName.equals("")) {
	retString = "<a id=\"zephyr-je-add-execute\" title=\"Execute Test\" class=\"toolbar-trigger viewissue-add-execute\" href=\"/secure/AddExecute!AddExecute.jspa?id=" + issueId + "\"><span class=\"trigger-label\">Execute new</span></a>";
} else if (!versionName.equals("") && (statusName.equals("UNEXECUTED") || statusName.equals("IN PROGRESS")) ) {
	retString = "<a href=\"/secure/enav/#/" + executionId + "\"><span class=\"trigger-label\">Continue exec</span></a>";
} else {
	retString = "<a id=\"zephyr-je-add-execute\" title=\"Execute Test\" class=\"toolbar-trigger viewissue-add-execute\" href=\"/secure/AddExecute!AddExecute.jspa?id=" + issueId + "\"><span class=\"trigger-label\">Execute new</span></a>";
}

return(retString)
