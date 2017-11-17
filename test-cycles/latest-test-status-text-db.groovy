import com.atlassian.jira.component.ComponentAccessor
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface

import java.sql.Connection

enableCache = {-> false}

if (issue.getIssueType().name.equals("Test"))) {
  return ""
}

def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
String helperName = delegator.getGroupHelperName("default");
StringBuffer sb = new StringBuffer()

String projectId = issue.getProjectId().toString()
String issueId = issue.getId().toString()


def sqlStmt = "SELECT schedule.status FROM ao_7deabf_schedule schedule, projectversion version where schedule.project_id=" + projectId + " and schedule.issue_id=" + issueId + " and schedule.version_id = version.id order by schedule.date_created desc limit 1";

Connection conn = ConnectionFactory.getConnection(helperName);
Sql sql = new Sql(conn)

def coloredStatus = ""

def statusName = ""
try {
  sql.eachRow(sqlStmt) {
    sb << "${it.status}"
    statusName = status[sb.toString()][0]
  }
} finally {
  sql.close()
}

return statusName
