<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">

<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script
	src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
<script src="scripts/ajax.js"></script>
<link rel="stylesheet" href="styles/style.css">
<script type='text/javascript' src='/dwr/engine.js'></script>
<script type='text/javascript' src='/dwr/interface/Consumer.js'></script>
</head>

<body>
	<h1 class="header">${module}
		<span id="utilityTitle">${utility}</span><span id="version">${version}</span>
	</h1>
	<div class="container">

		<div class="row">
			<div class="col-md-4"></div>
			<div class="col-md-4"></div>
			<div id="release" class="col-md-4">
				<label>Release</label> <select id="releaseId"
					class="custom-select custom-select-sm">
					<c:forEach items="${releases}" var="rel">
						<option value="${rel.value}">${rel.name}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="row">
			<div class="col-md-8">
				<form id="myForm">
					<table id="tableId" class="table table-bordered">
						<thead>
							<tr>
								<th>Categories</th>
								<th>Rows to Insert</th>
								<th>Chunks</th>
								<th>Selection</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${list}" var="item">
								<tr>
									<td><button type="button"
											class="categoryButton btn btn-primary btn-xs">
											<c:out value="${item.name}" />
										</button> <!--  inner table start-->
										<table class="table tableInner table-bordered">
											<thead>
												<tr>
													<th>Property Name</th>
													<th>Property Value</th>
												</tr>
											</thead>
											<tbody>
												<c:forEach items="${item.properties}" var="prop">
													<tr class="${prop.active?'':'hide'}">
														<td class="prop" data-toggle="tooltip"
															title="${prop.desc}"><c:out value="${prop.name}" /></td>
														<td class="prop"><input class="numberWidth"
															type="text" value="${prop.value}"></td>
														<td><img
															class="${prop.optional?'deleteImage':'hide'}"
															src="/images/delete.png"></td>
													</tr>
												</c:forEach>
											</tbody>
										</table> <!--  inner table end --></td>
									<td><input class="numberWidth" type="text"
										value="${item.size}"><span id="${item.name}_id" class="percentage">0%</span></td>
									<td><input class="numberWidth" type="text"
										value="${item.chunk}"></td>
									<td><input type="checkbox"></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</form>
			</div>
			<div class="col-md-4 overflow-auto bg-light statusHeight"
				id="statusId"></div>


		</div>
		<div class="row">
			<div class="col-md-4"></div>
			<div class="col-md-4"></div>
			<div id="animation" class="col-md-4"><img src="/images/motion.gif"></div>
		</div>
	</div>

	<div class="midArea">
		<button id="clearId" type="button" class="btn btn-primary btn-lg">Reset</button>
		<button id="stopId" type="button" class="btn btn-danger btn-lg">Graceful
			Stop</button>
		<button id="runId" type="button" class="btn btn-success btn-lg">Run</button>
		<button id="faqId" type="button" class="btn blackButton btn-lg">Tip</button>

	</div>
	
	<script>
		// activate ajax    
		dwr.engine.setActiveReverseAjax(true);
		// Whether to send a notification when the page is not loaded
		dwr.engine.setNotifyServerOnPageUnload(true, true);
		// The processing method after the error occurs
		dwr.engine.setErrorHandler(function() {
		});
	</script>
</body>

</html>
