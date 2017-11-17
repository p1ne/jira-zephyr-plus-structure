import com.atlassian.jira.component.ComponentAccessor
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface

import java.sql.Connection

enableCache = {-> false}

if (!issue.getIssueType().name.equals("Test")) {
  return ""
}

def status = [
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
StringBuffer sb = new StringBuffer()

String projectId = issue.getProjectId().toString()
String issueId = issue.getId().toString()

def sqlStmt = "SELECT schedule.status FROM ao_7deabf_schedule schedule, projectversion version where schedule.project_id=" + projectId + " and schedule.issue_id=" + issueId + " and schedule.version_id = version.id order by schedule.date_created desc limit 1";

Connection conn = ConnectionFactory.getConnection(helperName);
Sql sql = new Sql(conn)

def coloredStatus = ""

try {
  sql.eachRow(sqlStmt) {
    sb << "${it.status}"
		def statusName = status[sb.toString()][0]
		def statusColor = status[sb.toString()][1]
		coloredStatus = "<div class=\"labels exec-status-container\"><dd style=\"background-color: " + statusColor + "\">" + statusName + "</dd></div>";
  }
} finally {
    sql.close()
}

return coloredStatus
