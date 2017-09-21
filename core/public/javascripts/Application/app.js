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

  $.post("/application/perDayOrderNum", function (r) {
    let title = r.title == undefined ? r["series"][0]["name"] : r.title;
    Highcharts.chart('pieChart1', {
      credits: {
        text: "EasyAcc",
        href: ""
      },
      chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
      },
      title: {
        text: title
      },
      tooltip: {
        pointFormat: '{point.y}/{point.total}: <b>{point.percentage:.1f}%</b>'
      },
      plotOptions: {
        pie: {
          allowPointSelect: true,
          cursor: 'pointer',
          dataLabels: {
            enabled: false
          },
          showInLegend: true,
          events: {
            click: function (event) {
              refreshTopFive(event.point.name);
            }
          }
        }
      },
      series: r["series"]
    });
  });

  refreshTopFive("AMAZON_DE");

  function refreshTopFive (market) {
    let div = $("#progress_by_market");
    div.mask();
    div.load("/application/topTenSkuByMarket", {market: market}, function () {
      div.unmask();
    });
  }

  $(document).on("click", "a[name='sku_href']", function (r) {
    getSales($(this).data("fid"));
  });

  function getSales (fid) {
    let sid = fid;
    $.post("/Application/ajaxUnit", {sid: sid}, function (r) {
      Highcharts.chart("ajaxUnitDiv", {
        credits: {
          text: 'EasyAcc',
          href: ''
        },
        title: {
          text: sid
        },
        legend: {
          enabled: true
        },
        navigator: {
          enabled: true
        },
        scrollbar: {
          enabled: false
        },
        rangeSelector: {
          enabled: true,
          buttons: [{
            type: 'week',
            count: 1,
            text: '1w'
          }, {
            type: 'month',
            count: 1,
            text: '1m'
          }],
          selected: 1
        },
        xAxis: {
          type: 'datetime',
          yAxis: {min: 0}
        },
        tooltip: {
          shared: true,
          crosshairs: true,
          xDateFormat: '%Y-%m-%d'
        },
        series: r['series']
      });
    });
  }

});
