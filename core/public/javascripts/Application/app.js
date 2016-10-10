$(() => {
  $('#cci').click(() => {
    $.post('/application/clearCache', {}, (r) => {
      if (r.flag) {
        alert('清理首页缓存成功');
      } else {
        alert('清理失败.');
      }
    });
  });
});
