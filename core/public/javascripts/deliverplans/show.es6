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