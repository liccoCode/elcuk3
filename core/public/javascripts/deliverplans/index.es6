$(() => {

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    let type = $(this).data("type");
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='13'><div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#data-table").mask();
      $("#div" + format_id).load($(this).data("url"), {id: id}, function () {
        $("#data-table").unmask();
      });
    }
  });

});