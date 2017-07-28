/**
 * Created by licco on 2016/11/30.
 */
$(() => {


  // 切换供应商, 自行查询目的地
  $("#outCooperator").change(function () {
    let id = $(this).val();
    if (id) {
      LoadMask.mask();
      $.get('/Cooperators/findById', {
        id: id
      }, function (r) {
        //目的地赋值
        $("#whouse").val(r.address);
        LoadMask.unmask();
      });
    }
  });

$("input[name$='outQty']").change(function () {
    let $input = $(this);
    let value = $input.val();
    let origin = $input.data('origin');
    if(isNaN(value)){
      noty({
        text: '请输入数字!',
        type: 'error'
      });
      $(this).val($(this).data('origin'));
      $(this).focus();
      return false;
    }
    if (value > origin) {
      noty({
        text: '出库数超过物料可用数!',
        type: 'error'
      });
      $(this).val($(this).data('origin'));
      $(this).focus();
      return false;
    }
  
  });

});
