import com.atlassian.jira.component.ComponentAccessor
import groovy.sql.Sql
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface
import java.sql.Connection
import com.atlassian.jira.issue.index.IssueIndexManager
import com.atlassian.jira.component.ComponentAccessor
import java.text.SimpleDateFormat;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.event.type.*;

// Installation-specific
def robotUser = "robot";
String projectId = "11111"; // Run reindex for this project

def events = ComponentAccessor.getIssueEventManager();

def user = ComponentAccessor.getCrowdService().getUser(robotUser);

def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface);
String helperName = delegator.getGroupHelperName("default");
StringBuffer sb = new StringBuffer();

SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

def sqlStmt = "";
long unixTime = System.currentTimeMillis();

use (groovy.time.TimeCategory) {
  sqlStmt = "SELECT issue_id FROM ao_7deabf_schedule where project_id = " + projectId + " and (date_created between '" + timestamp.format(10.minutes.ago) + "' and '" + timestamp.format(0.minutes.from.now) + "' or executed_on between '" + (unixTime - 10*60*1000).toString() + "' and '" + unixTime.toString() + "') order by date_created desc";
}

Connection conn = ConnectionFactory.getConnection(helperName);
Sql sql = new Sql(conn)

try {
  sql.eachRow(sqlStmt) {
    def issue = ComponentAccessor.getIssueManager().getIssueObject(it.issue_id.toLong());
    IssueIndexManager issueIndexManager = ComponentAccessor.getIssueIndexManager();
    issueIndexManager.reIndex(issue)
    events.dispatchEvent(EventType.ISSUE_UPDATED_ID, issue, user, false)
  }
} finally {
  sql.close()
}
