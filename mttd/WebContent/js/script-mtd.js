//------------------------------------- Inicio legado -------------------------------
// QuickSearch script for JabRef HTML export
// Version: 2.0
//
// Copyright (c) 2006-2008, Mark Schenk
//
// This software is distributed under a Creative Commons Attribution 3.0 License
// http://creativecommons.org/licenses/by/3.0/

// Some features:
// + optionally searches Abstracts and Reviews
// + allows RegExp searches
//   e.g. to search for entries between 1980 and 1989, type:  198[0-9]
//   e.g. for any entry ending with 'symmetry', type:  symmetry$
//   e.g. for all reftypes that are books: ^book$, or ^article$
//   e.g. for entries by either John or Doe, type john|doe
// + easy toggling of Abstract/Review/BibTeX

// Search settings
var searchAbstract = true;
var searchReview = true;

// Speed optimisation introduced some esoteric problems with certain RegExp searches
// e.g. if the previous search is 200[-7] and the next search is 200[4-7] then the search doesn't work properly until the next 'keyup'
// hence the searchOpt can be turned off for RegExp adepts
var searchOpt = true;

if (window.addEventListener) {
	window.addEventListener("load",initSearch,false); }
else if (window.attachEvent) {
	window.attachEvent("onload", initSearch); }

function initSearch() {
	// basic object detection
	if(!document.getElementById || !document.getElementsByTagName) { return; }
	if (!document.getElementById('qstable')||!document.getElementById('qs')) { return; }

	// find QS table and appropriate rows
	searchTable = document.getElementById('qstable');
	var allRows = searchTable.getElementsByTagName('tbody')[0].getElementsByTagName('tr');

	// split all rows into entryRows and infoRows (e.g. abstract, review, bibtex)
	entryRows = new Array();
	infoRows = new Array(); absRows = new Array(); revRows = new Array();

	for (var i=0, k=0, j=0; i<allRows.length;i++) {
		if (allRows[i].className.match(/entry/)) {
			entryRows[j++] = allRows[i];
		} else {
			infoRows[k++] = allRows[i];
			// check for abstract/review
			if (allRows[i].className.match(/abstract/)) {
				absRows.push(allRows[i]);
			} else if (allRows[i].className.match(/review/)) {
				revRows.push(allRows[i]);
			}
		}
	}

	//number of entries and rows
	numRows = allRows.length;
	numEntries = entryRows.length;
	numInfo = infoRows.length;
	numAbs = absRows.length;
	numRev = revRows.length;

	//find the query field
	qsfield = document.getElementById('qsfield');

	// previous search term; used for speed optimisation
	prevSearch = '';

	//find statistics location
	stats = document.getElementById('stat');
	setStatistics(-1);

	// creates the appropriate search settings
	createQSettingsDialog();

	// shows the searchfield
	document.getElementById('qs').style.display = 'block';
	document.getElementById('qsfield').onkeyup = testEvent;
}

function quickSearch(tInput){

	 if (tInput.value.length == 0) {
		showAll();
		setStatistics(-1);
		qsfield.className = '';
		return;
	} else {
		// only search for valid RegExp
		try {
			var searchText = new RegExp(tInput.value,"i")
			closeAllInfo();
			qsfield.className = '';
		}
		catch(err) {
			prevSearch = tInput.value;
			qsfield.className = 'invalidsearch';
			return;
		}
	}

	// count number of hits
	var hits = 0;

	// start looping through all entry rows
	for (var i = 0; cRow = entryRows[i]; i++){

		// only show search the cells if it isn't already hidden OR if the search term is getting shorter, then search all
		// some further optimisation is possible: if the search string is getting shorter, and the row is already visible, skip it. Then be careful with hits!
		if(!searchOpt || cRow.className.indexOf('noshow')==-1 || tInput.value.length <= prevSearch.length){
			var found = false;

			var inCells = cRow.getElementsByTagName('td');
			var numCols = inCells.length;

			for (var j=0; j<numCols; j++) {
				cCell = inCells[j];
				var t = cCell.innerText?cCell.innerText:getTextContent(cCell);
				if (t.search(searchText) != -1){
					found=true;
					break;
				}
			}

			// look for further hits in Abstract and Review
			if(!found) {
				var articleid = cRow.id;
				if(searchAbstract && (abs = document.getElementById('abs_'+articleid))) {
					if (getTextContent(abs).search(searchText) != -1){ found=true; }
				}
				if(searchReview && (rev = document.getElementById('rev_'+articleid))) {
					if (getTextContent(rev).search(searchText) != -1){ found=true; }
				}
			}

			if(found) {
				cRow.className = 'entry show';
				hits++;
			} else {
				cRow.className = 'entry noshow';
			}
		}
	}

	// update statistics
	setStatistics(hits)

	// set previous search value
	prevSearch = tInput.value;
}

function toggleInfo(articleid,info) {

	var entry = document.getElementById(articleid);
	var abs = document.getElementById('abs_'+articleid);
	var rev = document.getElementById('rev_'+articleid);
	var bib = document.getElementById('bib_'+articleid);

	// Get the abstracts/reviews/bibtext in the right location
	// in unsorted tables this is always the case, but in sorted tables it is necessary.
	// Start moving in reverse order, so we get: entry, abstract,review,bibtex
	if (searchTable.className.indexOf('sortable') != -1) {
		if(bib) { entry.parentNode.insertBefore(bib,entry.nextSibling); }
		if(rev) { entry.parentNode.insertBefore(rev,entry.nextSibling); }
		if(abs) { entry.parentNode.insertBefore(abs,entry.nextSibling); }
	}

	if (abs && info == 'abstract') {
		if(abs.className.indexOf('abstract') != -1) {
		abs.className.indexOf('noshow') == -1?abs.className = 'abstract noshow':abs.className = 'abstract';
		}
	} else if (rev && info == 'review') {
		if(rev.className.indexOf('review') != -1) {
		rev.className.indexOf('noshow') == -1?rev.className = 'review noshow':rev.className = 'review';
		}
	} else if (bib && info == 'bibtex') {
		if(bib.className.indexOf('bibtex') != -1) {
		bib.className.indexOf('noshow') == -1?bib.className = 'bibtex noshow':bib.className = 'bibtex';
		}
	} else {
		return;
	}

	// check if one or the other is available
	var revshow = false;
	var absshow = false;
	var bibshow = false;
	(abs && abs.className.indexOf('noshow') == -1)? absshow = true: absshow = false;
	(rev && rev.className.indexOf('noshow') == -1)? revshow = true: revshow = false;
	(bib && bib.className == 'bibtex')? bibshow = true: bibshow = false;

	// highlight original entry
	if(entry) {
		if (revshow || absshow || bibshow) {
		entry.className = 'entry highlight show';
		} else {
		entry.className = 'entry show';
		}
	}

	// When there's a combination of abstract/review/bibtex showing, need to add class for correct styling
	if(absshow) {
		(revshow||bibshow)?abs.className = 'abstract nextshow':abs.className = 'abstract';
	}
	if (revshow) {
		bibshow?rev.className = 'review nextshow': rev.className = 'review';
	}

}

function setStatistics (hits) {
	if(hits < 0) { hits=numEntries; }
	if(stats) { stats.firstChild.data = hits + '/' + numEntries}
}

function getTextContent(node) {
	// Function written by Arve Bersvendsen
	// http://www.virtuelvis.com

	if (node.nodeType == 3) {
	return node.nodeValue;
	} // text node
	if (node.nodeType == 1) { // element node
	var text = [];
	for (var chld = node.firstChild;chld;chld=chld.nextSibling) {
		text.push(getTextContent(chld));
	}
	return text.join("");
	} return ""; // some other node, won't contain text nodes.
}

function showAll(){
	// first close all abstracts, reviews, etc.
	closeAllInfo();

	for (var i = 0; i < numEntries; i++){
		entryRows[i].className = 'entry show';
	}
}

function closeAllInfo(){
	for (var i=0; i < numInfo; i++){
		if (infoRows[i].className.indexOf('noshow') ==-1) {
			infoRows[i].className = infoRows[i].className + ' noshow';
		}
	}
}

function testEvent(e){
	if (!e) var e = window.event;
	quickSearch(this);
}

function clearQS() {
	qsfield.value = '';
	quickSearch(qsfield);
}

function redoQS(){
	showAll();
	quickSearch(qsfield);
}

// Create Search Settings
function toggleQSettingsDialog() {

	var qssettings = document.getElementById('qssettings');

	if(qssettings.className.indexOf('active')==-1) {
		qssettings.className = 'active';

		if(absCheckBox && searchAbstract == true) { absCheckBox.checked = 'checked'; }
		if(revCheckBox && searchReview == true) { revCheckBox.checked = 'checked'; }

	} else {
		qssettings.className= '';
	}
}

function createQSettingsDialog(){
	var qssettingslist = document.getElementById('qssettings').getElementsByTagName('ul')[0];

	if(numAbs!=0) {
		var x = document.createElement('input');
		x.id = "searchAbs";
		x.type = "checkbox";
		x.onclick = toggleQSetting;
		var y = qssettingslist.appendChild(document.createElement('li')).appendChild(document.createElement('label'));
		y.appendChild(x);
		y.appendChild(document.createTextNode('buscar nos resumos'));
	}
	if(numRev!=0) {
		var x = document.createElement('input');
		x.id = "searchRev";
		x.type = "checkbox";
		x.onclick = toggleQSetting;
		var y = qssettingslist.appendChild(document.createElement('li')).appendChild(document.createElement('label'));
		y.appendChild(x);
		y.appendChild(document.createTextNode('search reviews'));
	}

	// global variables
	absCheckBox = document.getElementById('searchAbs');
	revCheckBox = document.getElementById('searchRev');

	// show search settings
	if(absCheckBox||revCheckBox) {
		document.getElementById('qssettings').style.display = 'block';
	}
}

function toggleQSetting() {
	if(this.id=='searchAbs') { searchAbstract = !searchAbstract; }
	if(this.id=='searchRev') { searchReview = !searchReview; }
	redoQS()
}

// Sort Table Script
// Version: 1.1
//
// Copyright (c) 2006-2008, Mark Schenk
//
// This software is distributed under a Creative Commons Attribution 3.0 License
// http://creativecommons.org/licenses/by/3.0/

// NB: slow as molasses in FireFox, especially when sorting columns with a lot of text.
// An optimization is implemented which makes speed bearable, toggled by the following variable
var SORT_SPEED_OPT = true;
// a bit of browser preference: Opera does not need optimization
if(window.opera) { SORT_SPEED_OPT=false; }
// the optimization has one limitation on the functionality: when sorting search
// results, the expanded info, e.g. bibtex/review, is collapsed. In the non-optimized
// version they remain visible.


if (window.addEventListener) {
	window.addEventListener("load",initSortTable,false) }
else if (window.attachEvent) {
	window.attachEvent("onload", initSortTable); }

function initSortTable() {
var alltables = document.getElementsByTagName('table');
for(i=0;i<alltables.length;i++) {
	var currentTable = alltables[i];
	if(currentTable.className.indexOf('sortable') !=-1) {
		var thead = currentTable.getElementsByTagName('thead')[0];
		thead.title = 'Clique no cabe�alho da coluna para ordenar';
		for (var i=0;cell = thead.getElementsByTagName('th')[i];i++) {
			cell.onclick = function () { resortTable(this); };
			// make it possible to have a default sort column
			if(cell.className.indexOf('sort')!=-1) {
				resortTable(cell)
			}
		}
	}
}
}

var SORT_COLUMN_INDEX

function resortTable(td) {
	var column = td.cellIndex;
	var table = getParent(td,'TABLE');

	var allRows = table.getElementsByTagName('tbody')[0].getElementsByTagName('tr');
	var newRows = new Array();

	for (var i=0, k=0; i<allRows.length;i++) {

		var rowclass = allRows[i].className;

		if (rowclass.indexOf('entry') != -1) {
	       	newRows[k++] = allRows[i];
		}

		if (SORT_SPEED_OPT) {
		// remove highlight class
		allRows[i].className = rowclass.replace(/highlight/,'');
		// close information
		if(rowclass.indexOf('entry') == -1 && rowclass.indexOf('noshow') == -1) { allRows[i].className = rowclass + ' noshow';}
		}
	}


	// If other sort functions are deemed necessary (e.g. for
	// dates and currencies) they can be added.
	var sortfn = ts_sort_firstchild_caseinsensitive;
	SORT_COLUMN_INDEX = column;
	newRows.sort(sortfn);

	// create a container for showing sort arrow
	var arrow =  td.getElementsByTagName('span')[0];
	if (!arrow) { var arrow = td.appendChild(document.createElement('span'));}

	if (td.className) {
		if (td.className.indexOf('sort_asc') !=-1) {
			td.className = td.className.replace(/_asc/,"_des");
			newRows.reverse();
			arrow.innerHTML = '&uArr;';
		} else if (td.className.indexOf('sort_des') !=-1) {
			td.className = td.className.replace(/_des/,"_asc");
			arrow.innerHTML = '&dArr;';
		} else {
			td.className += ' sort_asc';
			arrow.innerHTML = '&dArr;';
		}
	} else {
		td.className += 'sort_asc';
		arrow.innerHTML = '&dArr;';
	}

	// Remove the classnames and up/down arrows for the other headers
	var ths = table.getElementsByTagName('thead')[0].getElementsByTagName('th');
	for (var i=0; i<ths.length; i++) {
		if(ths[i]!=td && ths[i].className.indexOf('sort_')!=-1) {
		// argh, moronic JabRef thinks (backslash)w is an output field!!
		//ths[i].className = ths[i].className.replace(/sort_(backslash)w{3}/,"");
		ths[i].className = ths[i].className.replace(/sort_asc/,"");
		ths[i].className = ths[i].className.replace(/sort_des/,"");

		// remove span
		var arrow =  ths[i].getElementsByTagName('span')[0];
		if (arrow) { ths[i].removeChild(arrow); }
		}
	}

	// We appendChild rows that already exist to the tbody, so it moves them rather than creating new ones
	for (i=0;i<newRows.length;i++) {
		table.getElementsByTagName('tbody')[0].appendChild(newRows[i]);

		if(!SORT_SPEED_OPT){
		// moving additional information, e.g. bibtex/abstract to right locations
		// this allows to sort, even with abstract/review/etc. still open
		var articleid = newRows[i].id;

		var entry = document.getElementById(articleid);
		var abs = document.getElementById('abs_'+articleid);
		var rev = document.getElementById('rev_'+articleid);
		var bib = document.getElementById('bib_'+articleid);

		var tbody = table.getElementsByTagName('tbody')[0];
		// mind the order of adding the entries
		if(abs) { tbody.appendChild(abs); }
		if(rev) { tbody.appendChild(rev); }
		if(bib) { tbody.appendChild(bib); }
		}
	}
}

function ts_sort_firstchild_caseinsensitive(a,b) {
	// only search in .firstChild of the cells. Speeds things up tremendously in FF
	// problem is that it won't find all the text in a cell if the firstChild is an element
	// or if there are other elements in the cell. Risky fix, but the speed boost is worth it.
	var acell = a.cells[SORT_COLUMN_INDEX];
	var bcell = b.cells[SORT_COLUMN_INDEX];

	acell.firstChild? aa = getTextContent(acell.firstChild).toLowerCase():aa = "";
	bcell.firstChild? bb = getTextContent(bcell.firstChild).toLowerCase():bb = "";

	if (aa==bb) return 0;
	if (aa<bb) return -1;
	return 1;
}

function ts_sort_caseinsensitive(a,b) {
	aa = getTextContent(a.cells[SORT_COLUMN_INDEX]).toLowerCase();
	bb = getTextContent(b.cells[SORT_COLUMN_INDEX]).toLowerCase();
	if (aa==bb) return 0;
	if (aa<bb) return -1;
	return 1;
}

function ts_sort_default(a,b) {
	aa = getTextContent(a.cells[SORT_COLUMN_INDEX]);
	bb = getTextContent(b.cells[SORT_COLUMN_INDEX]);
	if (aa==bb) return 0;
	if (aa<bb) return -1;
	return 1;
}

function getParent(el, pTagName) {
	if (el == null) {
		return null;
	} else if (el.nodeType == 1 && el.tagName.toLowerCase() == pTagName.toLowerCase()) {
		return el;
	} else {
		return getParent(el.parentNode, pTagName);
	}
}
//------------------------------------- Fim legado -------------------------------

function ativarAba(aba){
	get(aba).className="menu-cabecalho-link menu-selecionado";
}

function exibir(_p){
	get(_p).style.display="block";
}

function ocultar(_p){
	get(_p).style.display="none";
}

function get(_p){
	return document.getElementById(_p);
}

function executarForm(form, action, p){
	var f = get(form);
	
	f.action = action;
	
	if(p != null){
		var parametro = get(p).cloneNode(true);
		f.appendChild(parametro);
	}
	
	f.method='POST';
	f.submit();
}

function suggest(event, div, termo, tipoSugestao){
	if(!ignorarTeclasSugestao(event)){
		var xmlhttp = null;
		if (window.XMLHttpRequest){// code for IE7+, Firefox, Chrome, Opera, Safari
			xmlhttp = new XMLHttpRequest();
		}else{// code for IE6, IE5
			xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
		}
		
		if(xmlhttp == null){
			return ;
		}
		
		xmlhttp.onreadystatechange = function(){
			if (xmlhttp.readyState == 4 && xmlhttp.status == 200){
				div.innerHTML = xmlhttp.responseText;
			}
		}
		
		var solicitacao = "./suggest";
		xmlhttp.open("POST",solicitacao,true);
		xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");  
		
		var dados = encodeURIComponent('{"termo":"'+btoa(termo)+'","tipo_sugestao":"'+tipoSugestao+'"}');
		xmlhttp.send('dados='+dados);
	}
}

function ignorarTeclasSugestao(event){
	if(
			event.keyCode == 16 || event.keyCode == 17 
			|| event.keyCode == 18 || event.keyCode == 20 || event.keyCode == 27 
			|| event.keyCode == 35 ||event.keyCode == 36 || event.keyCode == 37 
			|| event.keyCode == 38 || event.keyCode == 39 || event.keyCode == 40
	){
		
		return true;
	}
	
	return false;
}


function limparSugestao(event, texto, divSugestao, inputTermoBusca){
	if(event.keyCode === 27 || (event.keyCode === 8 && texto == '')){
		divSugestao.innerHTML = "";
		inputTermoBusca.value = "";
	}
}

function mudarCorFundo(elemento, cor) {
	elemento.style.background = cor;
}