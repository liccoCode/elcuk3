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

});