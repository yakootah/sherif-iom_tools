<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">
<head>
    <script type='text/javascript' src='/dwr/engine.js'></script>
    <script type='text/javascript' src='/dwr/interface/DemoDwr.js'></script>
    
</head>
<body>

<script>
function getmessage(data){
    alert(data);
}
</script>

<input type="button" onclick="wow()" value="wow" >

	<c:url value="/resources/text.txt" var="url"/>
	<spring:url value="/resources/text.txt" htmlEscape="true" var="springUrl" />
	Spring URL: ${springUrl} at ${time}
	<br>
	JSTL URL: ${url}
	<br>
	Message: ${message}
</body>

</html>
