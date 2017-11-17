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

def componentManager = ComponentManager.getInstance()
def cfManager = ComponentManager.getInstance().getCustomFieldManager()
def coveredByField = cfManager.getCustomFieldObject(coveredByFieldId);

def jsonSlurper = new JsonSlurper()

retStr = ""

def reqMap = [:]
def reqList = []

if (coveredByField.getValue(issue) != null) {
  def object = jsonSlurper.parseText(coveredByField.getValue(issue))

  if (object.size()>0) {
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
      retStr += key + " "
    }
  }
}

return retStr
