/**
 * Created by Even on 2017/6/19
 */
$(() => {

 $("input[name$='outQty']").change(function () {
    let $input = $(this);
    let id = $(this).data("id");
    let value = $input.val();
    let origin = $input.data('origin');
    if (value > origin) {
      noty({
        text: '出库数超过计划数量!',
        type: 'error'
      });
      $(this).val($(this).data('origin'));
      $(this).focus();
      return false;
    }

    if (value <= 0) {
      noty({
        text: '出库数不能小于等于0!',
        type: 'error'
      });
      $(this).val($(this).data('origin'));
      $(this).focus();
      return false;
    }

    if ($(this).val()) {
      $.post('/MaterialOutbounds/updateUnit', {
        id: id,
        value: value
      }, (r) => {
        if (r) {
          noty({
            text: '更新实际出库数成功!',
            type: 'success'
          });
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    }
  });


  $("#deleteBtn").click(function (e) {
    if ($("#data_table input[type='checkbox']:checked").length == 0) {
      noty({
        text: "请选择需要解除的物料出库计划！",
        type: 'error'
      });
      return false;
    }
    if (confirm("确认解除选中的物料出库计划吗？")) {
      let ids = [];
      $("#data_table input[type='checkbox']:checked").each(function () {
        ids.push($(this).val());
      });
      $.post("/MaterialOutbounds/deleteUnit", {ids: ids}, function (r) {
        if (r) {
          $("#data_table input[type='checkbox']:checked").each(function () {
            $(this).parent("td").parent("tr").remove();
          });
          noty({
            text: "解除计划成功!",
            type: 'success'
          });
        } else {
          noty({
            text: "解除计划失败，请稍后再试，或者联系管理员!",
            type: 'error'
          });
        }
      });
    }
  });


  //快速添加物料编码js处理
  $('#addPlanUnitBtn').click(function (e) {
    e.stopPropagation();
    let code = $("#code").val() ;
    //实际交货数量
    if (code == '' || code == null) {
      noty({
        text: '请输入物料编码!',
        type: 'error'
      });
      return;
    }

    $("#unit_code").val(code);
    $("#addunits_form").submit();

  });


  function fidCallBack () {
    return {
      fid: $("input[name='rid']").val(),
      p: 'OUTBOUND'
    }
  }
  let dropbox = $('#dropbox');
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);

});
