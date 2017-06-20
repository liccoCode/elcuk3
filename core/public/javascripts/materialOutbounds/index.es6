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


  $("#confirmOutboundBtn").click(function (e) {
    e.stopPropagation();
    let num = $("input[name='ids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要出库的数据!',
        type: 'error'
      });
    } else {
      let i = 0;
      let ids = [];
      $("input[name='ids']:checked").each(function () {
        if ($(this).attr("status") != "Create") {
          i++;
          noty({
            text: $(this).val() + '已经出库，请选择【已创建】的出库单',
            type: 'error'
          });
          return false;
        }
        ids.push($(this).val());
      });
      if (i == 0) {
        $.post('/MaterialOutbounds/validMaterialOutboundQty', {ids: ids}, function (re) {
          if (re.flag) {
            $("#submit_form").submit();
          } else {
            if (confirm(re.message + "的可用库存与计划出库数量不一致，确认出库吗？")) {
              $("#submit_form").submit();
            }
          }
        });
      }
    }
  });


});