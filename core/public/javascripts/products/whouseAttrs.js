$(() => {
  $('#whouse_attrs_form').on('click', '#save_whouse_atts_btn', function () {
    const form = $(this).parents('form');
    LoadMask.mask(form);
    $.ajax(form.attr('action'), {
      type: 'POST',
      data: form.serialize(),
      dataType: 'json'
    }).done(function (r) {
      let msg;
      if (r.flag) {
        msg = {
          text: "保存成功.",
          type: 'success',
          timeout: 5000
        }
      } else {
        msg = {
          text: "#{r.message}",
          type: 'error',
          timeout: 5000
        }
      }
      noty(msg);
      LoadMask.unmask(form)
    })
  });

  $('#search_form').on('click', '#search_btn', function () {
    const btn = $(this);
    btn.parents('form').attr('action', btn.data('href')).submit();
  });

  $(document).on('click', '#whouse_attrs_attach_btn', function () {
    const file_home = $('#file_home');
    $.post('/attachs/uploadForBase64', {
      p: 'PRODUCTWHOUSE',
      fid: file_home.data('fid'),
      base64File: file_home.data('base64_file'),
      originName: file_home.data('origin_name')
    }).done(function (r) {
      alert(r.message);
      window.location.reload();
    })
  });

  var whouseAttachsFidCallBack = function () {
    return {
      fid: $("input[name='pro.sku']").val(),
      p: 'PRODUCTWHOUSE'
    };
  };
  var attachsLoder = function () {
    const dropbox = $('#whouse_attrs_dropbox');
    window.dropUpload.loadImages(whouseAttachsFidCallBack()['fid'], dropbox, whouseAttachsFidCallBack()['p'], 'span1');
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
