$(() => {

  function fidCallBack () {
    return {
      fid: $('#deliverymentId').val(),
      p: 'MATERIALPLAN'
    }
  }
  let dropbox = $('#dropbox');
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);

});