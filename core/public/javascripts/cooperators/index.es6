$(() => {

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let url = $(this).data("url");
    if ($("#div" + id).html() != undefined) {
      tr.next("tr").remove();
    } else {
      showTr(tr, id, url)
    }
  });

  function showTr (tr, id, url) {
    let html = "<tr style='background-color:#F2F2F2'><td colspan='12'>";
    html += "<div id='div" + id + "'></div></td></tr>";
    tr.after(html);
    $("#div" + id).load(url, {id: id});
  }

});