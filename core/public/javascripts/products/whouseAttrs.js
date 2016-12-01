$(() => {
  var whouseAttachsFidCallBack = function () {
    return {
      fid: $('#p_sku').val(),
      p: 'PRODUCTWHOUSE'
    };
  };
  var attachsLoder = function () {
    const dropbox = $('#whouseAttrsDropbox');
    window.dropUpload.loadImages(whouseAttachsFidCallBack()['fid'], dropbox, whouseAttachsFidCallBack()['p'], 'span1');
    window.dropUpload.iniDropbox(whouseAttachsFidCallBack, dropbox);
  };

  var typeaheader = function () {
    $("input[name='p.search']").typeahead({
      source: function (query, process) {
        $.get('/products/source', {search: query}).done(function (c) {
          process(c);
        })
      }
    });
  };

  var localResizer = function () {
    const lr = new LocalResize(document.getElementById('file_home'), {});
    lr.success(function (stop, data) {
      const file_home = $('#file_home').data('base64_file', data['base64Clean']).data('origin_name', data['original']['name']);
      stop()
    });
  };

  $(document).ready(function () {
    attachsLoder();
    typeaheader();
    localResizer();
  });
});
