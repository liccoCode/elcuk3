$(() => {
  function fidCallBack () {
    return {
      fid: $('#dropbox').attr('paymentId'),
      p: 'PAYMENTS'
    }
  }
  let dropbox = $('#dropbox');
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);

});