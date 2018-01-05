$(() => {

  $("#plan_update_btn").click(function (e) {
    let $form = $("#plan_form");
    LoadMask.mask();
    $.post("/deliverplans/update", $form.serialize(), function (r) {
      if (r.flag) {
        noty({
          text: "保存成功.",
          type: 'success',
          timeout: 5000
        })
      } else {
        noty({
          text: r.message,
          type: 'error',
          timeout: 5000
        })
      }
      LoadMask.unmask();
    });
  });

  $("#del_unit_form_submit").click(function (e) {
    e.preventDefault();
    let num = $("input[name='pids']:checked").length;
    if (num === 0) {
      noty({
        text: '请选择需要解除的出货单元!',
        type: 'error'
      });
    } else {
      LoadMask.mask();
      $('#bulkpost').attr('action', $(this).data('url')).submit();
    }
  });

  function fidCallBack () {
    return {
      fid: $('#deliverymentId').val(),
      p: 'DELIVERYMENT'
    }
  }

  let dropbox = $("#delivery_plan_dropbox");
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);

});