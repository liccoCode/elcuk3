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

});
