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

  $.post("/Application/perDayOrderNum", function (r) {
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
    div.load("/Application/topTenSkuByMarket", {market: market}, function () {
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
          formatter: function () {
            let s = "<b>" + Highcharts.dateFormat('%Y-%m-%d', point.x) + "</b><br>";
            point.points.forEach(function (point) {
              let totalY = point.series.yData.reduce(function (a, b) {
                a + b
              });
              s += "<span style='color:" + point.series.color + "'>" + point.series.name + "</span>: <b>#{point.y}" +
              " ({totalY})</b><br/>";
              return s;
            });
          }
        },
        series: r['series']
      });
    });
  }

});
