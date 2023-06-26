// On these events : Comment created

def issueKey = issue.key

def response = get('/rest/api/3/issue/'+ issueKey)
        .header('Content-Type','application/json')
        .asObject(Map)

response.body.fields.issuelinks.each {it ->

    if(it.inwardIssue && it.type.name == 'Cloners'){ 
        // it.type.name to limit the inwardIssue 
        logger.info("Inward issue: "+ it.inwardIssue.key)
        logger.info("Type Name: "+ it.type.name)
        // Add a comment
        addComment(issueKey,it.inwardIssue.key)
    }
}

def addComment (originalIssueKey, linkedIssueKey){

    // Get all comments from the original issue
    def commentsResponse = get("/rest/api/3/issue/${originalIssueKey}/comment")
        .header('Content-Type', 'application/json')
        .asObject(Map)
        
    // Extract the last comment from the response
    // Jira Cloud returns comments in order from oldest to newest
    def comments = commentsResponse.body.comments
    def lastComment = comments ? comments.last().body : ""
    
    // Post the last comment to the linked issue
    def commentResp = post("/rest/api/3/issue/${linkedIssueKey}/comment")
        .header('Content-Type', 'application/json')
        .body([
            body: lastComment
        ])
        .asObject(Map)
}
