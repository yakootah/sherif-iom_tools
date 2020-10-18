
var rowColor = 'pink';
var name;
var size;
var chunk;

function stop(data) {
	$('#runId').prop('disabled', false);
	displayMessage(data);
}
function updateStatus(data, percentage, id) {
	appendStatus(data);
	updatePercentage(percentage , id);
	scrollToBottomOfStatus();
}
$(document).ready(function() {
	document.getElementById('runId').onclick = runAjax;
	document.getElementById('clearId').onclick = clearAjax;
	document.getElementById('stopId').onclick = stopAjax;
	document.getElementById('faqId').onclick = showFaq;
	$('.categoryButton').on("click", function(e) {
		toggleDisplayProperties(e);
	});
	$('.deleteImage').on("click", function(e) {
		deleteRecord(e);
	});
	handleButtonsDisability(false);
	setDefaultRelease();
});

function runAjax() {
    var release = $('#releaseId').find(":selected").val();
	handleButtonsDisability(true);
	resetPercentage();
	$('#animation').show();
	Consumer.run(populateCategories(), release,  function (result){
		handleButtonsDisability(false);
		$('#animation').hide();
		displayMessage(result);
	});
}
function clearAjax() {

    document.getElementById("myForm").reset();
	$('#statusId').empty();
	showHiddenProperties();
	resetPercentage();
}
function resetPercentage()
{
	$('.percentage').each(function () { $(this).text("0%")});
}

function showHiddenProperties()
{
	var length = $('.tableInner').length;
	for (var i = 0; i < length; i++)
	{
		var propertiesLength = $($($('.tableInner')[i].children).get(1).children).length;
		for (var j = 0; j < propertiesLength; j++)
		{
			$($($($('.tableInner')[i].children).get(1).children).get(j)).show();
		}
	}
}

function stopAjax() {
	    var release = $('#releaseId').find(":selected").val();
		Consumer.stopProcessing(release, function(result) {
			handleButtonsDisability(false);
			displayMessage(result);
	});
}
function populateCategories() {

	let arr = new Array();
	var length = document.getElementById('tableId').rows.length;
	for (var i = 1; i < length; i++) 
	{
		var row = document.getElementById('tableId').rows.item(i);
		var p = { name: $(row.cells.item(0).childNodes[0])[0].innerText, 
		size: $(row.cells.item(1).childNodes[0]).val(), 
		chunk: $(row.cells.item(2).childNodes[0]).val(),
		selected: $(row.cells.item(3).children[0]).prop ('checked'),
		properties: populateProperties(row)};
		arr.push(p);
	}
	return arr;
}
function populateProperties(row) {

	let props = new Array();

	var length = $(row).find('.prop').length;
	for (var i = 0; i < length; i = i+2) 
	{
		var p = { name: $(row).find('.prop').get(i).textContent, 
		value: $(row).find('.prop').get(i + 1).childNodes[0].value};
		props.push(p);
	}
	return props;
}
function appendStatus(status) {
	var p = document.createElement("p");
	p.innerHTML = status;
	document.getElementById("statusId").appendChild(p);
	$(document.getElementById("statusId").lastChild).css('background-color', toggleColor());
}
function updatePercentage(data , id) {
	document.getElementById(id).innerHTML = data;
}
function scrollToBottomOfStatus() {
	document.querySelector('#statusId').scrollTop = 900;
}
function toggleColor() {
	if (rowColor == 'cyan') {
		rowColor = 'pink';
		return 'pink';
	}
	else {
		rowColor = 'cyan';
		return 'cyan';
	}

}
function displayMessage(result) {
	if (result.length > 0) {
		alert(result);
	}
}
function handleButtonsDisability(runState) {
	if (runState == true) {
		$('#runId').prop('disabled', true);
		$('#stopId').prop('disabled', false);
	}
	else {
		$('#runId').prop('disabled', false);
		$('#stopId').prop('disabled', true);

	}
}
function toggleDisplayProperties(e) {

	$($(e.target).siblings()[0]).toggle();
}
function deleteRecord(e) {
	
	var k = $(e.target).parent().parent().parent().children();
	var token = $(e.target).parent().parent().children(0).text();
	var searchToken = token.substring (0,token.indexOf('_'));

	for (var i = 0; i < k.length; i++) { 
    if(k[i].cells.item(0).textContent.includes(searchToken))
    {
		 $(k[i]).children(0).children(0).val(null);
         $(k[i]).hide();
    }
}
}
function setDefaultRelease()
{
	$('#releaseId').val($('#releaseId option:first').val());
}
function showFaq()
{
	alert("To reset your dockered db, run the following command: \n docker-compose up --force-recreate --no-deps --build oracle");
}