/**
 * Created by licco on 2016/12/5.
 */
$(() => {
  $("#confirmRefundBtn").click(function(e) {
    e.stopPropagation();
    let num = $("input[name='ids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要退货的数据!',
        type: 'error'
      });
    } else if (confirm("确认退货 " + num + " 条退货单吗?")) {
        $("#submit_form").submit();
    }
  });

});

