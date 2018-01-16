$(() => {

  function fidCallBack () {
    return {
      fid: $('#p_sku').val(),
      p: 'SKU'
    }
  }

  let dropbox = $('#dropbox');
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);

  $("#upAndDownForm").on("click", "#sellingUp, #sellingDown", function (e) {
    e.preventDefault();
    LoadMask.mask();
    let $btn = $(this);
    let flag = $btn.attr("id") === "sellingUp" ? true : false;
    $.post("/sellings/changeSellingType", {
      sellingId: $("#sellingId").val(),
      flag: flag
    }, function (r) {
      let msg;
      if (r.flag) {
        $("#sellingState").val("SELLING");
        msg = {
          text: "#{r.message} 系统上架成功",
          type: 'success'
        };
      } else if (!r.flag) {
        $("#sellingState").val("DOWN");
        msg = {
          text: "#{r.message} 系统下架成功",
          type: 'warning'
        };
      } else {
        msg = {
          text: r.message,
          type: 'error'
        };
      }
      LoadMask.unmask();
      noty(msg);
    });
  });

});