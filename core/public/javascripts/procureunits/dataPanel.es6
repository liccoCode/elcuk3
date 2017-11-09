$(() => {

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
            },
            afterAnimate: function (event) {
              if ($("#brandname").val() !== 'EASYACC') {
                refreshTopFive(r["highestMarket"]);
              }
            }
          }
        }
      },
      series: r["series"]
    });
  });

  function refreshTopFive (market) {
    let div = $("#progress_by_market");
    div.mask();
    div.load("/application/topTenSkuByMarket", {market: market}, function () {
      div.unmask();
      let fid = $("#progress_by_market").find("div").data("fid");
      getSales(fid);
    });
  }

  function getSales (fid) {
    let sid = fid;
    $.post("/application/ajaxUnit", {sid: sid}, function (r) {
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

  $.getJSON("/deliveryments/indexPer", function (r) {
    $("#a_units").highcharts('StockChart', {
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: '每日下单量, 2012-2017'
      },
      subtitle: {
        text: 'Source: easya.cc'
      },
      legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'middle'
      },
      scrollbar: {
        barBackgroundColor: 'gray',
        enabled: true
      },
      rangeSelector: {
        enabled: true,
        selected: 2
      },
      xAxis: {
        title: {
          text: 'daily num'
        },
        type: 'datetime'
      },
      yAxis: {
        title: {
          text: 'Number of Employees'
        },
        min: 0
      },
      plotOptions: {
        series: {
          pointStart: 2000
        }
      },
      tooltip: {
        Shared: false,
        shadow: true
      },
      series: r['series']
    });
  });

  $.getJSON("/deliveryments/perCreateTotalNum", function (r) {
    $("#a_turn").highcharts({
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: 'Per Total ProcureUnit Num. 2012-2017'
      },

      series: r['series']
    });
  });

  $.getJSON("/payments/queryPerDayAmount", function (r) {
    $("#payment_div").highcharts('StockChart', {
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: '每日支付请款单总额'
      },
      rangeSelector: {
        enabled: true,
        selected: 2
      },
      xAxis: {
        title: {
          text: 'daily num'
        },
        type: 'datetime'
      },
      series: r['series']
    });

  });

});