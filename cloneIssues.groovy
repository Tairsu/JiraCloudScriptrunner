def mappings = [
  [from: 'Support', to: 'ServiceDesk']       
]

for (mapping in mappings) {
  def fromKey = mapping.from
  def toKey = mapping.to
  
  // Restrict this script to only run against issues in the "from" project
  if (issue.fields.project.key != fromKey) {
    continue
  }

  // Create a new issue in the "to" project using the current issue as a template
  def newIssue = post("/rest/api/3/issue")
      .header('Content-Type', 'application/json')
      .body([
          fields: [
              project: [key: toKey],
              summary: issue.fields.summary,
              description: issue.fields.description,
              issuetype: [name: issue.fields.issuetype.name],
              //priority: [name: issue.fields.priority.name],
              assignee: issue.fields.assignee ? [name: issue.fields.assignee.name] : null
          ]
      ])
      .asObject(Map)

  assert newIssue.status == 201

  // Link the original issue and the new issue
  def linkType = "Cloners"
  def linkUrl = "/rest/api/3/issueLink"
  def linkData = [
      type: [
          name: linkType
      ],
      inwardIssue: [
          key: newIssue.body.key
      ],
      outwardIssue: [
          key: issue.key
      ]
  ]
  def linkResponse = post(linkUrl)
      .header('Content-Type', 'application/json')
      .body(linkData)
      .asObject(Map)

  assert linkResponse.status == 201
