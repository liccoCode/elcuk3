$(() => {
  $('table[name=fbacenterList]').on('click', 'a[name=enableAutoSync], a[name=disableAutoSync]', function () {
    if (!confirm('确认操作?')) return;

    const $a = $(this);
    const $img = $($a.prev('span').find('img'));
    const $masker = $a.parents('tr');

    LoadMask.mask($masker);
    $.post(`/fbacenters/${$a.data('centerid')}/${$a.attr('name')}`).done((r) => {
      if (r.flag) {
        if ($a.attr('name') == 'enableAutoSync') {//当前为 Close 状态
          $img.attr('src', '/img/green.png');
          $a.attr('name', 'disableAutoSync').text('Disable').attr('data-original-title', '禁用自动同步');
        } else if ($a.attr('name') == 'disableAutoSync') {//当前为 Open 状态
          $img.attr('src', '/img/red.png');
          $a.attr('name', 'enableAutoSync').text('Enable').attr('data-original-title', '启用自动同步');
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
  }).on('click', 'a[name=checkFBALabel]', function () {
    const $tds = $(this).parents('tr').find('td');
    $('#fba_ship_to_body').html(new FbaShipToBuilder($tds).buildBody());
    $('#fba_ship_to_modal').modal('show');
  });

  class FbaShipToBuilder {
    constructor (tds) {
      this.addressLine1 = $(tds[1]).text();
      this.addressLine2 = $(tds[2]).text();
      this.city = $(tds[3]).text();
      this.name = $(tds[4]).text();
      this.countryCode = $(tds[5]).text();
      this.stateOrProvinceCode = $(tds[6]).text();
      this.postalCode = $(tds[7]).text();
    }

    static get countryCodeMap () {
      return {
        "GB": "United Kingdom",
        "US": "United States",
        "CA": "Canada",
        "CN": "China (Mainland)",
        "DE": "Germany",
        "FR": "France",
        "IT": "Italy",
        "JP": "Japan"
      }
    };

    buildBody () {
      var body = "<b> SHIP TO:</b><br>";
      if (!_.isEmpty(this.name)) {
        body += `<b>${this.name}</b><br>`;
      }
      if (!_.isEmpty(this.addressLine1)) {
        body += `<b>${this.addressLine1}</b><br>`;
      }
      if (!_.isEmpty(this.addressLine2)) {
        body += `<b>${this.addressLine2}</b><br>`;
      }
      if (!_.isEmpty(this.city)) {
        body += `<b>${this.city},</b>&nbsp;`;
      }
      if (!_.isEmpty(this.stateOrProvinceCode)) {
        body += `<b>${this.stateOrProvinceCode}</b>&nbsp;`;
      }
      if (!_.isEmpty(this.postalCode)) {
        body += `<b>${this.postalCode}</b><br>`;
      }
      if (!_.isEmpty(this.countryCode)) {
        body += `<b>${this.formatCountryCode()}</b><br>`;
      }
      return body;
    }

    formatCountryCode () {
      return FbaShipToBuilder.countryCodeMap[this.countryCode]
    }
  }

  $(document).ready(function () {
    $('table[name=fbacenterList]').dataTable({
      "aaSorting": [[0, "asc"]],
      "bPaginate": false,
      "bLengthChange": false,
      "bFilter": true,
      "bInfo": false,
      "bAutoWidth": false
    })
  });
});
