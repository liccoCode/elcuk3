$(() => {
  $("form[name='data-form']").ajaxForm({
    dataType: 'json',
    success (r) {
      alert(r.message);
      window.location.reload();
    }
  });

  // 设置关闭和打开用户后页面相关的展示
  $(document).on('setCloseOrOpen', '#openUser,#closeUser', function () {
    const $a = $(this);
    const imgid = $a.data('userid');
    const $img = $(`#${imgid}`);

    if ($a.attr('id') === 'openUser') {
      $a.attr('id', 'closeUser').text('Close').attr('data-original-title', '关闭此账户');
      $img.attr('src', '/img/green.png');
    } else if ($a.attr('id') === 'closeUser') {
      $img.attr('src', '/img/red.png');
      $a.attr('id', 'openUser').text('Open').attr('data-original-title', '打开此账户');
    }
  });

  // 关闭用户
  $(document).on('click', '#closeUser', function () {
    if (!confirm('确认关闭该用户吗?')) {
      return;
    }
    const $a = $(this);
    const id = $a.data('userid');
    LoadMask.mask();

    $.post(`/users/${id}/closeUser`, (r) => {
      try {
        if (r.flag) {
          noty({
            text: r.message,
            type: 'success',
            timeout: 3000
          });
          $a.trigger('setCloseOrOpen');
        } else {
          noty({
            text: r.message,
            type: 'error',
            timeout: 3000
          });
        }
      } finally {
        LoadMask.unmask();
      }
    });
  });

  // 打开用户
  $(document).on('click', '#openUser', function () {
    if (!confirm('确认打开该用户吗?')) return;

    const $a = $(this);
    const id = $a.data('userid');
    LoadMask.mask();

    $.post(`/users/${id}/openUser`, (r) => {
      try {
        if (r.flag) {
          noty({
            text: r.message,
            type: 'success',
            timeout: false,
            closeWith: 'button',
            buttons: [{
              addClass: 'btn btn-link',
              text: 'close',
              onClick ($noty) {
                return $noty.close();
              }
            }]
          });
          $a.trigger('setCloseOrOpen');
        } else {
          noty({
            text: r.message,
            type: 'error',
            timeout: 3000
          });
        }
      } finally {
        LoadMask.unmask();
      }
    });
  });

  $(document).on('change', ':checkbox', function () {
    const $o = $(this);
    const id = $o.data('userid');
    $o.parents('table').find(`:checkbox[class='${id}menu${$o.attr('value')}']`).prop('checked', $o.prop('checked'));
    $o.parents('table').find(`:checkbox[class='${id}menu${$o.attr('value')}']`).trigger('change');
  });

  $("a[name='select_all_category']").click(function (e) {
    e.preventDefault();
    $(this).parent().parent().parent().find("input[type='checkbox']").each(function () {
      $(this).prop("checked", !$(this).prop("checked"));
    });
  });
});
