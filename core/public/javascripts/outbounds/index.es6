$(() => {

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    let type = $(this).data("type");
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='14'><div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      if (type == 'Normal' || type == 'B2B') {
        $("#div" + format_id).load("/Outbounds/showProcureUnitList", {id: id});
      } else {
        $("#div" + format_id).load("/Outbounds/showStockRecordList", {id: id});
      }
    }
  });

  $("a[name='checkPage']").click(function () {
    $("#whichPage").val($(this).attr("page"));
    if ($(this).attr("page") == 'otherBtn') {
      $("#data_table input[type='checkbox']").each(function () {
        $(this).prop("checked", false);
      });
    } else {
      $("#other_table input[type='checkbox']").each(function () {
        $(this).prop("checked", false);
      });
    }
  });

  $("#" + $("#whichPage").val()).click();

  $("#exportExcel").click(function (e) {
    e.stopPropagation();
    let $form = $("#search_Form");
    window.open($(this).data("url") + "?" + $form.serialize(), "_blank");
  });

});