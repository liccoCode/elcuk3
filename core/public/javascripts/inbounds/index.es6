/**
 * Created by licco on 2016/12/12.
 */
$(() => {

  $("#printBtn").click(function(e) {
    e.stopPropagation();
    if ($("input[type='checkbox']:checked").length == 0) {
      noty({
        text: '请选择需要打印的入库单',
        type: 'error'
      });
      return;
    }
    let $form = $("#inboundForm");
    window.open("/Inbounds/printQuaternionForm?" + $form.serialize(), "_blank");
  });


  $("td[name='clickTd']").click(function() {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr><td colspan='13'><hr><div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + format_id).load("/Inbounds/showProcureUnitList", {id: id});
    }
  });

});


