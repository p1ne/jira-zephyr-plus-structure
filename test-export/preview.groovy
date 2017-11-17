import com.atlassian.event.api.EventPublisher
import com.atlassian.jira.ComponentManager
import com.atlassian.jira.issue.CustomFieldManager
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory
import com.atlassian.jira.config.properties.ApplicationProperties
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer
import com.atlassian.jira.issue.fields.CustomFieldImpl
import org.ofbiz.core.entity.ConnectionFactory
import org.ofbiz.core.entity.DelegatorInterface
import groovy.sql.Sql
import java.sql.Connection
import groovy.json.JsonSlurper

// Installation-specific
def coveredByFieldId = "customfield_11200";   // Covered by tests - ToDo list custom field

def delegator = (DelegatorInterface) ComponentAccessor.getComponent(DelegatorInterface)
String helperName = delegator.getGroupHelperName("default");

StringBuffer sb0 = new StringBuffer()
StringBuffer sb1 = new StringBuffer()
StringBuffer sb2 = new StringBuffer()
StringBuffer sb3 = new StringBuffer()

eventPublisher = (EventPublisher) ComponentAccessor.getComponent(EventPublisher)
velocityRequestContextFactory = (VelocityRequestContextFactory) ComponentAccessor.getComponent(VelocityRequestContextFactory)
applicationProperties = (ApplicationProperties) ComponentAccessor.getComponent(ApplicationProperties)
wikiRenderer = new AtlassianWikiRenderer(eventPublisher, applicationProperties, velocityRequestContextFactory);

def componentManager = ComponentManager.getInstance()
def cfManager = ComponentManager.getInstance().getCustomFieldManager()
def coveredByField = cfManager.getCustomFieldObject(coveredByFieldId);

retStr = ""

retStr += "</td></tr><tr style=\"width: 100%;\"><td colspan=\"10\" style=\"font-size: 10pt;\">"
retStr += wikiRenderer.render(issue.getDescription(), null)
retStr += "<br/>"

def sqlStmt = "SELECT count(order_id) as cnt FROM ao_7deabf_teststep where issue_id = " + issue.getId().toString()

Connection conn = ConnectionFactory.getConnection(helperName);
Sql sql = new Sql(conn)

def stepsCount = 0

try {
  sql.eachRow(sqlStmt) {
    sb0 << "${it.cnt}"
    stepsCount = sb0.toInteger()
  }
  sb0.setLength(0)

  if (stepsCount>0) {
    retStr += "<table class=\"aui jira-restfultable jira-restfultable-allowhover\">"
    retStr += "<tr>"
    retStr += "<th>#</td>"
    retStr += "<th>Test Step</td>"
    retStr += "<th>Test Data</td>"
    retStr += "<th>Expected Result</td>"
    retStr += "</tr>"

		def sqlStmt2 = "SELECT order_id, step, data, result FROM ao_7deabf_teststep where issue_id = " + issue.getId().toString() + " order by order_id"

		try {
      sql.eachRow(sqlStmt2) {
        sb0 << "${it.order_id}"
        sb1 << "${it.step}"
        sb2 << "${it.data}"
        sb3 << "${it.result}"

        def order =			!sb0.toString().equals("null") ? wikiRenderer.render(sb0.toString(), null) : "-"
        def testStep = 		!sb1.toString().equals("null") ? wikiRenderer.render(sb1.toString(), null) : "-"
        def testData = 		!sb2.toString().equals("null") ? wikiRenderer.render(sb2.toString(), null) : "-"
        def testResult =	!sb3.toString().equals("null") ? wikiRenderer.render(sb3.toString(), null) : "-"


        sb0.setLength(0)
        sb1.setLength(0)
        sb2.setLength(0)
        sb3.setLength(0)

        retStr += "<tr>"
        retStr += "<td>" + order + "</td>"
        retStr += "<td>" + testStep + "</td>"
        retStr += "<td>" + testData + "</td>"
        retStr += "<td>" + testResult + "</td>"
        retStr += "</tr>"
      }
  	} catch (e) { }
    retStr += "</table>"
  }
} finally {
  sql.close()
}

def jsonSlurper = new JsonSlurper()

def reqMap = [:]
def reqList = []

if (coveredByField.getValue(issue) != null) {
	def object = jsonSlurper.parseText(coveredByField.getValue(issue))

	if (object.size()>0) {
		retStr += "<br/>"

		retStr += "<table>"
		retStr += "<th>Covered requirements</th>"

		for (i=0;i<object.size();i++) {
      reqTokens = object[i].id.tokenize(" ")
      reqType = reqTokens[0]
      reqSection = reqTokens[1]

      reqList = reqMap.get(reqType)
      if ((reqList) == null) {
        reqList=[reqSection]
      } else {
        if (!reqList.contains(reqSection)) {
        	reqList.add(reqSection)
          reqList.sort()
        }
      }
      reqMap.put(reqType, reqList)
    }
    for (key in reqMap.keySet()) {
      retStr += "<tr><td>" + key + ": "
      for (req in reqMap.get(key)) {
        retStr += req + ", "
      }
      retStr = retStr.substring(0, retStr.length() - 2 )
      retStr += "</td></tr>"
    }
    retStr += "</table>"
	}
}

return retStr
