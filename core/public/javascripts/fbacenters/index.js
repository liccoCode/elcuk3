$(() => {
  $('table[name=fbacenterList]').on('click', 'a[name=enableAutoSync], a[name=disableAutoSync]', function () {
    if (!confirm('确认关闭该用户吗?')) return;

    const $a = $(this);
    const $img = $($a.prev('span').find('img'));
    const $masker = $a.parents('tr');

    LoadMask.mask($masker);
    $.post(`/fbacenters/${id}/${$a.attr('id') === 'enableAutoSync' ? 'enableAutoSync' : 'disableAutoSync'}`, (r)
    ).done((r) => {
      if (r.flag) {
        if ($a.attr('id') === 'enableAutoSync') {//当前为 Close 状态
          $img.attr('src', '/img/green.png');
          $a.attr('id', 'disableAutoSync').text('Disable').attr('data-original-title', '禁用自动同步');
        } else if ($a.attr('id') === 'disableAutoSync') {//当前为 Open 状态
          $img.attr('src', '/img/red.png');
          $a.attr('id', 'enableAutoSync').text('Enable').attr('data-original-title', '启用自动同步');
        }
        noty({
          text: "更新成功!",
          type: 'success',
          timeout: 3000
        });
      }
    }).fail((r) => {
      noty({
        text: "更新 FBACenter 时出现未知异常, 请联系开发部门处理.",
        type: 'error',
        timeout: 3000
      });
    });
    LoadMask.unmask($masker);
  });
});
