$(() => {

  $("td[name='clickTd']").click(function() {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr><td colspan='13'><div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + format_id).load("/Outbounds/showProcureUnitList", {id: id});
    }
  });

});