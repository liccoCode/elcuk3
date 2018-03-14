/**
 * Created by even on 2016/11/14.
 */
$(() => {


  //点击明细修改按钮，显示弹出框,并初始化明细数据
  $("a[name='unitUpdateBtn']").click(function (e) {
    let id = $(this).attr("uid");
    let $input = $(this);
    $.get('/Elcuk/editJson', {
      id: id
    }, function (r) {
      //赋值
      $("#configId").val(r.id);
      $("#configType").text(r.type);
      $("#configParamcode").text(r.paramcode);
      $("#configName").text(r.name);
      $("#configVal").val(r.val);
      $("#bom_modal").modal('show')
    });
  });

  //提交修改明细功能
  $("#submitUpdateBtn").click(function () {
    $("#updateUnit_form").submit();
  });

});

