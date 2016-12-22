/**
 * Created by licco on 2016/12/2.
 */
$(() => {

  $("#new_inbound input[name$='qty']").change(function() {
    if ($(this).val() == $(this).data('qty')) {
      $(this).parent('td').next().find('select').hide();
    } else {
      $(this).parent('td').next().find('select').show();
    }
  });

  $("#deleteBtn").click(function(e) {
    e.stopPropagation();
    if ($("input[name$='unitId']:checked").length == 0) {
      noty({
        text: "请选择需要解除的采购计划！",
        type: 'error'
      });
      return false;
    }
    if ($("input[name$='unitId']:checkbox").not("input:checked").length == 0) {
      noty({
        text: "如果需要全部删除采购计划，请点击【取消】按钮!",
        type: 'error'
      });
      return false;
    }

    if (confirm("确认解除选中采购计划吗？")) {
      $("input[name$='unitId']:checked").each(function() {
        $(this).parent("td").parent("tr").remove();
      });
    }
  });

});