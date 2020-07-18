package com.tory.demo.diskcache

/**
 * Author: xutao
 * Version V1.0
 * Date: 2020/7/12
 * Description:
 * Modification History:
 * Date Author Version Description
 * -----------------------------------------------------------------------------------
 * 2020/7/12 xutao 1.0
 * Why & What is modified:
 */
const val DATA_STR_1 = """
    DATA_STR_1
    {
	"data": {
		"banner": [{
			"advId": 3967,
			"image": "https://w1.hoopchina.com.cn/feedback_api/9f/3c/c7/9f3cc722d71d659ef4d3405086af22cf002.png",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5f06ebf1d6e4e792a7b4bd8a",
			"hupuAdId": 3967,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5f06ebf1d6e4e792a7b4bd8a",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3987,
			"image": "https://w2.hoopchina.com.cn/feedback_api/04/c5/ae/04c5ae8d960d792f3cc747941c451acd001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.dewu.com%2Fnezha%2Fdetail%2F5f07e09ce18ef9d85e283a9f",
			"hupuAdId": 3987,
			"key": 5,
			"val": "https://m.dewu.com/nezha/detail/5f07e09ce18ef9d85e283a9f",
			"requestId": "",
			"cn": ""
		}]
    }
"""


const val DATA_STR = """
    DATA_STR
    {
	"data": {
		"banner": [{
			"advId": 3967,
			"image": "https://w1.hoopchina.com.cn/feedback_api/9f/3c/c7/9f3cc722d71d659ef4d3405086af22cf002.png",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5f06ebf1d6e4e792a7b4bd8a",
			"hupuAdId": 3967,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5f06ebf1d6e4e792a7b4bd8a",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3987,
			"image": "https://w2.hoopchina.com.cn/feedback_api/04/c5/ae/04c5ae8d960d792f3cc747941c451acd001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.dewu.com%2Fnezha%2Fdetail%2F5f07e09ce18ef9d85e283a9f",
			"hupuAdId": 3987,
			"key": 5,
			"val": "https://m.dewu.com/nezha/detail/5f07e09ce18ef9d85e283a9f",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3895,
			"image": "https://w1.hoopchina.com.cn/feedback_api/96/14/83/9614836c06fedfb39df34fff8254fd96001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5eff14f2e4934992ae0bd639",
			"hupuAdId": 3895,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5eff14f2e4934992ae0bd639",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3955,
			"image": "https://w1.hoopchina.com.cn/feedback_api/40/d5/b3/40d5b3559ea784caf1d498ea26f0d47e001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5f05a305d6e4e792a7b4bce5",
			"hupuAdId": 3955,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5f05a305d6e4e792a7b4bce5",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3954,
			"image": "https://w1.hoopchina.com.cn/feedback_api/15/06/7d/15067d36f1485b183b0d8410e3950e3b001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5f028c7398c3ba929dd79ab5",
			"hupuAdId": 3954,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5f028c7398c3ba929dd79ab5",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3968,
			"image": "https://w3.hoopchina.com.cn/feedback_api/77/da/2f/77da2f71dd76646728f6f10f164d4b0f001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5f05a5f5e4934992ae0bd766",
			"hupuAdId": 3968,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5f05a5f5e4934992ae0bd766",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3969,
			"image": "https://w1.hoopchina.com.cn/feedback_api/69/53/55/69535534088f0a0dfcd82d80e4c24453001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fmdu%2FsellerNotice%2FsellerNotice.html%3FnoticeId%3D1365",
			"hupuAdId": 3969,
			"key": 5,
			"val": "https://m.poizon.com/mdu/sellerNotice/sellerNotice.html?noticeId=1365",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3906,
			"image": "https://w1.hoopchina.com.cn/feedback_api/28/a6/41/28a641255152100690711f2d845e0f8a002.png",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Frn-activity%2Fncee%2Floading%3Fpath%3Dpath20200714",
			"hupuAdId": 3906,
			"key": 5,
			"val": "https://m.poizon.com/rn-activity/ncee/loading?path=path20200714",
			"requestId": "",
			"cn": ""
		}, {
			"advId": 3976,
			"image": "https://w3.hoopchina.com.cn/feedback_api/63/af/81/63af81dc622dbe2854927ed34f896fc2001.jpg",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fducacaahbbbjcg%2F5f05a29ae4934992ae0bd74f",
			"hupuAdId": 3976,
			"key": 5,
			"val": "https://m.poizon.com/nezha/ducacaahbbbjcg/5f05a29ae4934992ae0bd74f",
			"requestId": "",
			"cn": ""
		}],
		"backgroundList": [],
		"hotList": [{
			"typeId": 4,
			"sellCalendar": {
				"sysDate": 1594523341,
				"images": ["https://du.hupucdn.com/FhosniSPbVx8O7u5X2sV_5Np07KE", "https://du.hupucdn.com/FnZRXYOnJ-r2q4J64QvX0SAGu-sM", "https://du.hupucdn.com/FtWdjQOHs9N97rxp7D_AZLR_eW6O", "https://du.hupucdn.com/FliZeWcrUgZqVM0MBkzoKPcgtq0u"]
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 49071,
				"logoUrl": "https://du.hupucdn.com/Fl3_W5BA3xwaRnuaVG15ra91QBGH",
				"title": "Nike Tech Hip Pack Big 邮差包单肩斜挎包  黑色",
				"subTitle": "",
				"price": 30900,
				"soldNum": 38802,
				"sourceName": "hottest",
				"articleNumber": "BA5751-010",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2019-03-01 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 100,
				"cn": "FB"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 56546,
				"logoUrl": "https://china-product.poizon.com/FipzNy1F4joB0dlPPJrflWslGZfmnew.png",
				"title": "CASIO 卡西欧 G-SHOCK 防水运动手表男表 GA-2100-1A1PR 黑色",
				"subTitle": "",
				"price": 162900,
				"soldNum": 2198,
				"sourceName": "hottest",
				"articleNumber": "GA-2100-1A1PR",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2019-08-31 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 100,
				"cn": "FDCF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 41161,
				"logoUrl": "https://china-product.poizon.com/FkiUlZB-2-FLbhykkj1bQcxoNafAnew.png",
				"title": "JBL TUNE 110BT 入耳式无线蓝牙运动颈挂式耳机 带麦可通话 苹果安卓通用 黑色",
				"subTitle": "",
				"price": 24900,
				"soldNum": 1452,
				"sourceName": "hottest",
				"articleNumber": "JBL-T110BT黑",
				"preSellStatus": 1,
				"preSellDeliverTime": 1296000,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2018-10-01 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 100,
				"cn": "CLICK"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 59842,
				"logoUrl": "https://china-product.poizon.com/FqSNgiT7cAkZtpvuSaZGM3Dkey2vnew.png",
				"title": "CASIO 卡西欧 G-SHOCK EVERLAST 跨界合作款 背光灯Choice of Champions 防水防震功能手表 GBA-800EL-4A",
				"subTitle": "",
				"price": 134900,
				"soldNum": 383,
				"sourceName": "hottest",
				"articleNumber": "GBA-800EL-4A",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2019-06-30 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 100,
				"cn": "FDCF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 70955,
				"logoUrl": "https://du.hupucdn.com/Fs98XUPYw6yKxnPBqASQXks4LI85",
				"title": "Nike Zoom Vista Lite 白 女款",
				"subTitle": "",
				"price": 49900,
				"soldNum": 7844,
				"sourceName": "hottest",
				"articleNumber": "CI0905-100",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-02-16 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "ICF"
			}
		}, {
			"typeId": 2,
			"boutique": {
				"detail": {
					"id": 1000501,
					"title": "日韩表",
					"subTitle": "最新发售单品",
					"coverUrl": "",
					"bgColor": "",
					"detailCoverUrl": "",
					"detailTitle": "",
					"detailSubTitle": "",
					"url": "https://m.poizon.com/router/product/BoutiqueRecommendDetailPage?recommendId=1000501&spuIds=1044012,1046262,1046255,1046249,1046242,1049524"
				},
				"images": ["https://cdn.poizon.com/node-common/MTE1OTM2ODM1NTI1OTE=.jpg", "https://cdn.poizon.com/node-common/JUU0JUI4JUJCJUU1JTlCJUJFMTU5NDAyMTM1OTA1OQ==.jpg", "https://cdn.poizon.com/node-common/JUU0JUI4JUJCJUU1JTlCJUJFMTU5NDAyMTI2NjY5Ng==.jpg", "https://cdn.poizon.com/node-common/JUU0JUI4JUJCJUU1JTlCJUJFMTU5NDAyMTE5NTE5MQ==.jpg", "https://cdn.poizon.com/node-common/JUU0JUI4JUJCJUU1JTlCJUJFMTU5NDAyMTA4NzQ5Nw==.jpg", "https://cdn.poizon.com/node-common/MTE1OTQyMDA4NTgzNzg=.jpg"],
				"requestId": "",
				"cn": "FIA3"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 1049526,
				"logoUrl": "https://cdn.poizon.com/node-common/JUU0JUI4JUJCJUU1JTlCJUJFMTE1OTQyMDA4ODAwMjU=.jpg",
				"title": "CASIO 卡西欧 小表盘钢带防水日韩表 女款 白色 LTP-2064A-7A3VDF",
				"subTitle": "",
				"price": 55900,
				"soldNum": 1,
				"sourceName": "hottest",
				"articleNumber": "LTP-2064A-7A3VDF",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-04-30 00:00:00.000",
				"propertyValueId": 21552064,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 100,
				"cn": "FN"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 39216,
				"logoUrl": "https://china-product.poizon.com/FsAE0lPEq_SAjvUZir5Ew_sHWe1Rnew.png",
				"title": "adidas Yeezy Boost 350 V2 Cloud White 冰蓝",
				"subTitle": "",
				"price": 223900,
				"soldNum": 169148,
				"sourceName": "hottest",
				"articleNumber": "FW3043",
				"preSellStatus": 1,
				"preSellDeliverTime": 3024000,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2019-09-21 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "FC"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 10651,
				"logoUrl": "https://china-product.poizon.com/news_byte55054byte_e9e40b1cc39af51eb4a3aad7e21aaceb_w500h320newnew.jpg",
				"title": "Nike M2K Tekno 老爹鞋 女款 白橙",
				"subTitle": "",
				"price": 59900,
				"soldNum": 39494,
				"sourceName": "hottest",
				"articleNumber": "AO3108-001",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2018-05-10 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "UCF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 71854,
				"logoUrl": "https://china-product.poizon.com/Fm28jYNAFWBdLl-rivzx3seqRq25new.png",
				"title": "adidas originals Yeezy Boost 350 V2 Desert Sage 灰橙 侧透满天星 跑步鞋",
				"subTitle": "",
				"price": 167900,
				"soldNum": 53554,
				"sourceName": "hottest",
				"articleNumber": "FX9035",
				"preSellStatus": 1,
				"preSellDeliverTime": 2592000,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-03-28 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "ICF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 43761,
				"logoUrl": "https://china-product.poizon.com/FlCJbDGyZtijGk6Gxfp1mIGMco2bnew.png",
				"title": "【情侣款】Air Jordan 1 “Obsidian” (GS) 北卡蓝 黑曜石 篮球鞋 ",
				"subTitle": "",
				"price": 236900,
				"soldNum": 57237,
				"sourceName": "hottest",
				"articleNumber": "575441-140",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2019-08-17 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "FDCF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 69734,
				"logoUrl": "https://du.hupucdn.com/FkGdqIRE8-ZZQbHEF56_Pw6L5kTV",
				"title": "Nike Air Tailwind 79 幻影灰白",
				"subTitle": "",
				"price": 44900,
				"soldNum": 12272,
				"sourceName": "hottest",
				"articleNumber": "487754-100",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2019-10-31 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "UCF"
			}
		}, {
			"typeId": 2,
			"boutique": {
				"detail": {
					"id": 1000533,
					"title": "小白鞋人气推荐",
					"subTitle": "",
					"coverUrl": "",
					"bgColor": "",
					"detailCoverUrl": "https://du.hupucdn.com/Fqqr5MlbG68PeGu404kx2naOK8Wx",
					"detailTitle": "",
					"detailSubTitle": "",
					"url": "https://m.poizon.com/router/product/BoutiqueRecommendDetailPage?recommendId=1000533&spuIds=61894,78468,84420,54208,67803,24248"
				},
				"images": ["https://china-product.poizon.com/FoCVXWx6n_BxxLKnnQbSv5b2DnFZnew.png", "https://china-product.poizon.com/FjMb1K1W-CMok-_0IvZRZfV5dGkMnew.png", "https://china-product.poizon.com/FkfCjhiBtqdMhppztRGapk-ElZ_0new.png", "https://du.hupucdn.com/FnQUbqBqAW6BFUWSyOw2blqA__Fv", "https://china-product.poizon.com/FosS3XiczGpMdNTu55PvdXwfpHUanew.png", "https://china-product.poizon.com/FtPDH00mDuaJakQmOkc2gEhhAfrPnew.png"],
				"requestId": "",
				"cn": "FIA"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 1000846,
				"logoUrl": "https://china-product.poizon.com/node-common/NzUweDQ4MCVFNSU5QiVCRTExNTg1NjQ1MjI1NTA2newnew.jpg",
				"title": "Nike Sb Dunk Low Pro Blue Fury 蓝",
				"subTitle": "",
				"price": 130900,
				"soldNum": 15216,
				"sourceName": "hottest",
				"articleNumber": "BQ6817-400",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-04-01 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "CLICK"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 67304,
				"logoUrl": "https://china-product.poizon.com/Fuo9pnvKeeFJDtPpYAhnjkzL7chtnew.png",
				"title": "Air Jordan 1 Zoom Racer Blue 白蓝 \"小Dior\" 篮球鞋",
				"subTitle": "",
				"price": 197900,
				"soldNum": 28283,
				"sourceName": "hottest",
				"articleNumber": "CK6637-104",
				"preSellStatus": 1,
				"preSellDeliverTime": 2592000,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-03-14 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "ICF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 22220,
				"logoUrl": "https://china-product.poizon.com/FlHA8FIqH4ZbZbrBm2algLYYy5G1new.png",
				"title": "OFF-WHITE Stencil S/S Over Tee 红色喷漆箭头印花短袖T恤 宽松版型",
				"subTitle": "",
				"price": 156900,
				"soldNum": 1798,
				"sourceName": "hottest",
				"articleNumber": "OMAA038R191850151028",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2018-11-15 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 1,
				"cn": "UCF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 1000263,
				"logoUrl": "https://china-product.poizon.com/node-common/NzUweDQ4MCVFNSU5QiVCRTExNTg0MzU4MDM1OTgznewnew.jpg",
				"title": "Stussy x Nike Air Zoom Spiridon CG 2 沙漠黄  跑步鞋",
				"subTitle": "",
				"price": 376900,
				"soldNum": 9623,
				"sourceName": "hottest",
				"articleNumber": "CQ5486-200",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-04-03 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "CLICK"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 21120,
				"logoUrl": "https://cdn.poizon.com/node-common/MjExMjAxNTg3MzczODAwMzE1.jpg",
				"title": "OFF-WHITE 油画箭头棉质短袖T恤 宽松版型 男款 白色",
				"subTitle": "",
				"price": 137900,
				"soldNum": 4173,
				"sourceName": "hottest",
				"articleNumber": "OMAA038R191850120288",
				"preSellStatus": 0,
				"preSellDeliverTime": 2592000,
				"preSellLimitPurchase": 1,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2018-10-10 00:00:00.000",
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 1,
				"cn": "UCF"
			}
		}, {
			"typeId": 0,
			"product": {
				"spuId": 84135,
				"logoUrl": "https://cdn.poizon.com/node-common/MTE1OTM0MTcyOTU1MDU=.jpg",
				"title": "Air Jordan 1 High OG Smoke Grey 烟灰 \"小Union\"",
				"subTitle": "",
				"price": 135900,
				"soldNum": 4610,
				"sourceName": "hottest",
				"articleNumber": "555088-126",
				"preSellStatus": 0,
				"preSellDeliverTime": 0,
				"preSellLimitPurchase": 0,
				"isShowPreSellTag": 0,
				"isPreSellNew": 0,
				"isShow": 1,
				"isShowCrossTag": 0,
				"recommendRequestId": "1109013141739856",
				"sellDate": "2020-07-11 00:00:00.000",
				"productTagVo": {
					"title": "热度上涨",
					"type": 2,
					"imageUrl": "https://du.hupucdn.com/FpHuIWDOg0y9JZzGF7oy7NS7KW8R?width=168&height=168?width=undefined&height=undefined?width=undefined&height=undefined?width=undefined&height=undefined?width=undefined&height=undefined"
				},
				"propertyValueId": 0,
				"gifUrl": "",
				"isShowGif": 0,
				"priceType": 1,
				"goodsType": 0,
				"cn": "HP"
			}
		}],
		"seriesList": [{
			"advId": 175,
			"image": "http://du.hupucdn.com/FvSCvdlNOlFp119ExE-FZaiEScM9",
			"text": "热销榜单",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fdetail%2F5ef09b749c8d02fc9ebf05c4",
			"spuId": 28835,
			"key": 5,
			"val": "https://m.poizon.com/nezha/detail/5ef09b749c8d02fc9ebf05c4",
			"requestId": "1282149939024326656",
			"cn": "OKKO"
		}, {
			"advId": 155,
			"image": "http://du.hupucdn.com/Fvjh933KfK93169JPfziS9q-xB4A",
			"text": "新品速递",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fpreview%2F5ee8918387c040fc97d4290a",
			"spuId": 84135,
			"key": 5,
			"val": "https://m.poizon.com/nezha/preview/5ee8918387c040fc97d4290a",
			"requestId": "1282149939024326656",
			"cn": "OKKO"
		}, {
			"advId": 177,
			"image": "http://du.hupucdn.com/FkuqZlNRPMBnEq5kQH_mej95Nae6",
			"text": "限时抢购",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Frn-activity%2Fseckill",
			"spuId": 3185410,
			"key": 5,
			"val": "https://m.poizon.com/rn-activity/seckill",
			"requestId": "1282149939024326656",
			"cn": "OKKO"
		}, {
			"advId": 0,
			"image": "https://china-product.poizon.com/FnUkmnf1z25vrBJJbUaywlNGez4Gnew.png",
			"text": "潮流腕表专区",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Frouter%2Fproduct%2FBoutiqueRecommendDetailPage%3FrecommendId%3D663%26spuIds%3D34711",
			"themeId": 663,
			"spuId": 34711,
			"val": "",
			"requestId": "1282149939024326656",
			"cn": "N_PKKCL"
		}, {
			"advId": 0,
			"image": "https://china-product.poizon.com/FsAE0lPEq_SAjvUZir5Ew_sHWe1Rnew.png",
			"text": "Yeezy 系列精选",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Frouter%2Fproduct%2FBoutiqueRecommendDetailPage%3FrecommendId%3D75%26spuIds%3D39216",
			"themeId": 75,
			"spuId": 39216,
			"val": "",
			"requestId": "1282149939024326656",
			"cn": "N_PKKCL"
		}, {
			"advId": 0,
			"image": "https://china-product.poizon.com/FkFr_lo-0rAPL5hu5oMAKuVgkYcjnew.png",
			"text": "Jordan女款",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Frouter%2Fproduct%2FBoutiqueRecommendDetailPage%3FrecommendId%3D1000302%26spuIds%3D79638",
			"themeId": 1000302,
			"spuId": 79638,
			"val": "",
			"requestId": "1282149939024326656",
			"cn": "N_CKKPV2"
		}, {
			"advId": 0,
			"image": "https://china-product.poizon.com/news_byte95083byte_79e71043d9f5a4442d4f988d3706eb5d_w500h320newnew.jpg",
			"text": "帆布鞋",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Frouter%2Fproduct%2FBoutiqueRecommendDetailPage%3FrecommendId%3D1000463%26spuIds%3D9670",
			"themeId": 1000463,
			"spuId": 9670,
			"val": "",
			"requestId": "1282149939024326656",
			"cn": "N_CKKPV2"
		}, {
			"advId": 1,
			"image": "http://du.hupucdn.com/FjlALMBZuyhs57HvYbuyZdGCdMD9",
			"text": "MORE",
			"redirect": "https://m.poizon.com/router/product/ProductCategoryPageV2?index=1",
			"spuId": 3169542,
			"key": 18,
			"val": "0",
			"requestId": "1282149939024326656",
			"cn": "OKKO"
		}],
		"originPriceBuy": {
			"iconUrl": "https://du.hupucdn.com/Fq7B6QK0lTTLR196oNvvyqALTU44",
			"originalRaffleDtoList": [{
				"raffleId": 286,
				"title": "Air Jordan 1 Zoom \"小Dior\" 篮球鞋",
				"originPrice": 129900,
				"imageUrl": "https://du.hupucdn.com/FnOYHgEjex6bsBQvtkntr2XnpXIK"
			}, {
				"raffleId": 287,
				"title": "Converse 1970s 高帮复古黑",
				"originPrice": 14900,
				"imageUrl": "https://du.hupucdn.com/FotPCROhfXvRXJogZrxIyfOuF1ZB"
			}, {
				"raffleId": 288,
				"title": "Nike SB Dunk Low “ACG” 黑紫",
				"originPrice": 79900,
				"imageUrl": "https://du.hupucdn.com/FkNQzkyOoaCYRlQRMUzznEM2HVmi"
			}]
		},
		"actBanner": {
			"advId": 3957,
			"image": "https://w2.hoopchina.com.cn/feedback_api/82/a4/3a/82a43a08bbf28f7661bb8e73ca6ecb7e003.gif",
			"redirect": "https://m.poizon.com/router/web/BrowserPage?loadUrl=https%3A%2F%2Fm.poizon.com%2Fnezha%2Fducacaahbbbjcg%2F5f05a29ae4934992ae0bd74f",
			"hupuAdId": 3957,
			"key": 5,
			"val": "https://m.poizon.com/nezha/ducacaahbbbjcg/5f05a29ae4934992ae0bd74f",
			"requestId": "",
			"cn": ""
		},
		"bindBox": [],
		"timeRaffleIndex": {
			"tag": {
				"bgColor": "#2B2C3C",
				"fontColor": "#FFFFFF",
				"title": "0元参与 >"
			},
			"todayRaffles": [{
				"dateType": 0,
				"timeRaffleId": 797,
				"status": 1,
				"statusText": "进行中",
				"productId": 39216,
				"awardName": "adidas Yeezy Boost 350 V2 Cloud White 冰蓝",
				"awardCover": "https://china-product.poizon.com/FsAE0lPEq_SAjvUZir5Ew_sHWe1Rnew.png",
				"productType": 0
			}],
			"tomorrowRaffles": [{
				"dateType": 1,
				"timeRaffleId": 798,
				"status": 0,
				"statusText": "即将开始",
				"productId": 84135,
				"awardName": "Air Jordan 1 High OG Smoke Grey 烟灰",
				"awardCover": "https://cdn.poizon.com/node-common/MTE1OTM0MTcyOTU1MDU=.jpg",
				"productType": 0
			}]
		},
		"lastId": "1",
		"branding": {
			"newUser": true,
			"image": "https://du.hupucdn.com/FohmlHnM2B2oQnl7Fk7gv7wewNCe",
			"redirect": "https://m.poizon.com/nvwa/#/detail/5e68b4f56bf60823cbd92eea"
		},
		"newChannel": {
			"type": 0,
			"giftPackage": {
				"title": "新人专享礼包",
				"type": 0,
				"image": "https://cdn.poizon.com/node-common/JUU2JTk2JUIwJUU0JUJBJUJBJUU5JUEyJTkxJUU5JTgxJTkzMTU4NzAwMTk2NDc4Mw==.gif",
				"mediaType": 0,
				"endValidTime": 0,
				"ext": ["限时首单包邮", "新人折扣，每周四更新"],
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new&requireLogin=1",
				"showTag": true
			},
			"newSpuList": [{
				"spuId": 1026883,
				"title": "中国李宁 旷世奇才 运动时尚系列短袖文化衫 黑色  ",
				"image": "https://cdn.poizon.com/node-common/01ce550844d88d7d6c01d9d133f2c198.jpg",
				"mediaType": 0,
				"originalPrice": 13900,
				"discountPrice": 11900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D1026883&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 41882,
				"title": "Air Jordan HBR 男子篮球短裤 黑白",
				"image": "https://china-product.poizon.com/FoiCyR1qEHw0tZRCbjOSdNQYV72mnew.png",
				"mediaType": 0,
				"originalPrice": 27900,
				"discountPrice": 25900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D41882&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 12859,
				"title": "Nike LeBron James Icon Edition Swingman Los Angeles Lakers 詹姆斯 湖人 球迷版 球衣",
				"image": "https://cdn.poizon.com/node-common/MTE1ODc3MDEwMTQ3OTA=.jpg",
				"mediaType": 0,
				"originalPrice": 50900,
				"discountPrice": 48900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D12859&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 1009458,
				"title": "Nike DNA Summer Hoops 篮球运动抽绳短裤 男款 黑色",
				"image": "https://cdn.poizon.com/node-common/JUU0JUI4JUJCJUU1JTlCJUJFMTE1ODcxMjA5OTkzMDU=.jpg",
				"mediaType": 0,
				"originalPrice": 27900,
				"discountPrice": 25900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D1009458&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 28673,
				"title": "NIKE 洛杉矶湖人队（SW）詹姆斯 23号球衣",
				"image": "https://china-product.poizon.com/FoEmeUlPp9WbN7FR-7FtxRMmkhTKnew.png",
				"mediaType": 0,
				"originalPrice": 53900,
				"discountPrice": 51900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D28673&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 35519,
				"title": "Nike 男子 Swoosh logo宽松透气跑步运动短裤 黑色",
				"image": "https://cdn.poizon.com/node-common/21bc9903c9b9c85599febb523ece96c1.jpg",
				"mediaType": 0,
				"originalPrice": 23900,
				"discountPrice": 21900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D35519&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 81906,
				"title": "【李佳琦推荐】Air Jordan 1 Low “Laser Blue” 激光蓝 篮球鞋",
				"image": "https://china-product.poizon.com/FtaV4NcUCOfs1o8Rnx_C8Yaf9-aVnew.png",
				"mediaType": 0,
				"originalPrice": 62900,
				"discountPrice": 59800,
				"discountDesc": "新人立减31元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D81906&requireLogin=1",
				"tags": ["包邮", "新人专享9.5折"],
				"discountText": "立减31元"
			}, {
				"spuId": 77053,
				"title": "adidas originals Yeezy Boost 350 V2 “Flax” 亚麻 亚洲限定 跑步鞋",
				"image": "https://china-product.poizon.com/FsqKCcpX18dA4uL5HtxjKHudSu_dnew.png",
				"mediaType": 0,
				"originalPrice": 170900,
				"discountPrice": 160900,
				"discountDesc": "新人立减100元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D77053&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减100元"
			}, {
				"spuId": 2354,
				"title": "Nike Air Force 1 07 low 空军一号 白色 板鞋 315122-111",
				"image": "https://china-product.poizon.com/news_byte78328byte_910aebe686317410293c893e9fef6978_w500h320newnew.jpg",
				"mediaType": 0,
				"originalPrice": 55900,
				"discountPrice": 53200,
				"discountDesc": "新人立减27元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D2354&requireLogin=1",
				"tags": ["包邮", "新人专享9.5折"],
				"discountText": "立减27元"
			}, {
				"spuId": 8708,
				"title": "Vans Old Skool Black 黑白休闲板鞋 ",
				"image": "https://china-product.poizon.com/Fvs5m5OnLL6bWLL_PxJ6oTXnYcYfnew.png",
				"mediaType": 0,
				"originalPrice": 36900,
				"discountPrice": 33300,
				"discountDesc": "新人立减36元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D8708&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减36元"
			}, {
				"spuId": 71854,
				"title": "adidas originals Yeezy Boost 350 V2 Desert Sage 灰橙 侧透满天星 跑步鞋",
				"image": "https://china-product.poizon.com/Fm28jYNAFWBdLl-rivzx3seqRq25new.png",
				"mediaType": 0,
				"originalPrice": 167900,
				"discountPrice": 157900,
				"discountDesc": "新人立减100元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D71854&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减100元"
			}, {
				"spuId": 70936,
				"title": "Nike Court Vision 白色",
				"image": "https://china-product.poizon.com/Fp9CSF8OyTNS3YEoNp22oqXTg5o9new.png",
				"mediaType": 0,
				"originalPrice": 29900,
				"discountPrice": 27000,
				"discountDesc": "新人立减29元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D70936&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减29元"
			}, {
				"spuId": 9675,
				"title": "Nike Benassi JDI Mismatch 拖鞋",
				"image": "https://china-product.poizon.com/news_byte32611byte_40108489eddde40ca521604d10fdb877_w500h320newnew.jpg",
				"mediaType": 0,
				"originalPrice": 14900,
				"discountPrice": 12900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D9675&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 78504,
				"title": "Nike Air Max 270 React \"Bubble Pack\" 白绿",
				"image": "https://china-product.poizon.com/FtG4BCW52g1O6mBMJaGrFEZXclVYnew.png",
				"mediaType": 0,
				"originalPrice": 77900,
				"discountPrice": 73900,
				"discountDesc": "新人立减40元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D78504&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减40元"
			}, {
				"spuId": 78848,
				"title": "adidas originals Yeezy Boost 350 V2 “Cinder” 黑生胶",
				"image": "https://cdn.poizon.com/node-common/JUU2JTlDJUFBJUU2JUEwJTg3JUU5JUEyJTk4LTExNTg0NTAzMjQzMDcx.jpg",
				"mediaType": 0,
				"originalPrice": 222900,
				"discountPrice": 207900,
				"discountDesc": "新人立减150元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D78848&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减150元"
			}, {
				"spuId": 26409,
				"title": "Onitsuka Tiger 鬼冢虎 MEXICO 66 SLIP ON 懒人鞋一脚蹬",
				"image": "https://china-product.poizon.com/FjtLqy6pLu-gEGhz5_vPgU6amU8Enew.png",
				"mediaType": 0,
				"originalPrice": 35900,
				"discountPrice": 32400,
				"discountDesc": "新人立减35元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D26409&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减35元"
			}, {
				"spuId": 33834,
				"title": "匹克态极1.0PLUS 跑鞋 男子",
				"image": "https://china-product.poizon.com/FgNECTthFKzBrbibz8q6bUX-xnvxnew.png",
				"mediaType": 0,
				"originalPrice": 30900,
				"discountPrice": 27900,
				"discountDesc": "新人立减30元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D33834&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减30元"
			}, {
				"spuId": 1011006,
				"title": "Nike Sb Dunk Low Pro Laser Orange 紫金",
				"image": "https://cdn.poizon.com/node-common/NzUweDQ4MCVFNSU5QiVCRTExNTg3OTY4MTU4OTEz.jpg",
				"mediaType": 0,
				"originalPrice": 196900,
				"discountPrice": 186900,
				"discountDesc": "新人立减100元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D1011006&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减100元"
			}, {
				"spuId": 10903,
				"title": "Nike Air Monarch 4 White Navy",
				"image": "https://china-product.poizon.com/news_byte93277byte_10d2b3087d20040db8f8d537ddf876b0_w500h320newnew.jpg",
				"mediaType": 0,
				"originalPrice": 34900,
				"discountPrice": 31500,
				"discountDesc": "新人立减34元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D10903&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减34元"
			}, {
				"spuId": 1000517,
				"title": "Nike Kyrie 6 EP 篮球之星 白绿 篮球鞋",
				"image": "https://cdn.poizon.com/node-common/NzUweDQ4MCVFNSU5QiVCRTExNTg2NDE4MTg5Mjcz.jpg",
				"mediaType": 0,
				"originalPrice": 59900,
				"discountPrice": 57000,
				"discountDesc": "新人立减29元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D1000517&requireLogin=1",
				"tags": ["包邮", "新人专享9.5折"],
				"discountText": "立减29元"
			}, {
				"spuId": 10881,
				"title": "【薇娅推荐】Nike Benassi Duo Ultra Slide 拖鞋",
				"image": "https://china-product.poizon.com/news_byte48064byte_59abb4fc542481724e9b1e2b74bb8300_w500h321newnew.jpg",
				"mediaType": 0,
				"originalPrice": 19900,
				"discountPrice": 16000,
				"discountDesc": "新人立减39元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D10881&requireLogin=1",
				"tags": ["包邮", "新人专享8.0折"],
				"discountText": "立减39元"
			}, {
				"spuId": 10407,
				"title": "Nike Benassi JDI  黑 拖鞋",
				"image": "https://china-product.poizon.com/news_byte46634byte_b8e957be54bacf612828280ae5ee9da9_w500h320newnew.jpg",
				"mediaType": 0,
				"originalPrice": 14900,
				"discountPrice": 12000,
				"discountDesc": "新人立减29元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D10407&requireLogin=1",
				"tags": ["包邮", "新人专享8.0折"],
				"discountText": "立减29元"
			}, {
				"spuId": 26350,
				"title": "Nike Tanjun 男子 休闲跑鞋",
				"image": "https://china-product.poizon.com/FuykjexQ6q_23EBTg7aMVefZMBX1new.png",
				"mediaType": 0,
				"originalPrice": 30900,
				"discountPrice": 27900,
				"discountDesc": "新人立减30元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D26350&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减30元"
			}, {
				"spuId": 2355,
				"title": "Nike Air Force 1 07 女款 纯白 板鞋 315115-112",
				"image": "https://china-product.poizon.com/news_byte78328byte_edf8e57f7a2dac45d55d3d609b03c773_w500h320newnew.jpg",
				"mediaType": 0,
				"originalPrice": 52900,
				"discountPrice": 50300,
				"discountDesc": "新人立减26元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D2355&requireLogin=1",
				"tags": ["包邮", "新人专享9.5折"],
				"discountText": "立减26元"
			}, {
				"spuId": 11687,
				"title": "Converse One Star Ox Low Suede Black White 黑色 板鞋 158369C",
				"image": "https://china-product.poizon.com/news_byte78889byte_f2f644828e53a7f38d342f46cfc0f39c_w500h320newnew.jpg",
				"mediaType": 0,
				"originalPrice": 39900,
				"discountPrice": 36000,
				"discountDesc": "新人立减39元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D11687&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减39元"
			}, {
				"spuId": 39216,
				"title": "adidas Yeezy Boost 350 V2 Cloud White 冰蓝",
				"image": "https://china-product.poizon.com/FsAE0lPEq_SAjvUZir5Ew_sHWe1Rnew.png",
				"mediaType": 0,
				"originalPrice": 223900,
				"discountPrice": 208900,
				"discountDesc": "新人立减150元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D39216&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减150元"
			}, {
				"spuId": 69928,
				"title": "Nike Kyrie 6 CNY EP 欧文6 中国年配色",
				"image": "https://china-product.poizon.com/FiOFA7kbL9e2bwFMqYQZz47Sa2zPnew.png",
				"mediaType": 0,
				"originalPrice": 59900,
				"discountPrice": 55900,
				"discountDesc": "新人立减40元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D69928&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减40元"
			}, {
				"spuId": 38581,
				"title": "Converse Chuck Taylor 1970s Hi Top 粉色 高帮",
				"image": "https://china-product.poizon.com/FuSUlyiFmR8wuhy5OuZ7FUaaQDUQnew.png",
				"mediaType": 0,
				"originalPrice": 41900,
				"discountPrice": 39900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D38581&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}, {
				"spuId": 34750,
				"title": "李宁 驭帅11 男子减震耐磨防滑高帮篮球鞋 水蜜桃",
				"image": "https://china-product.poizon.com/FpN0rcsp419zRL-gT7Iu3EoH83Awnew.png",
				"mediaType": 0,
				"originalPrice": 38900,
				"discountPrice": 35100,
				"discountDesc": "新人立减38元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D34750&requireLogin=1",
				"tags": ["包邮", "新人专享9.0折"],
				"discountText": "立减38元"
			}, {
				"spuId": 71010,
				"title": "adidas originals Superstar J 白黑",
				"image": "https://china-product.poizon.com/FlhWByzA7qUB0LqufpxRx3HIVDAznew.png",
				"mediaType": 0,
				"originalPrice": 40900,
				"discountPrice": 38900,
				"discountDesc": "新人立减20元",
				"routerUrl": "https://m.poizon.com/router/web/BrowserPage?loadUrl=http%3A%2F%2Fm.poizon.com%2Fh5activity%2Fcoupon-newuser-new%3FspuId%3D71010&requireLogin=1",
				"tags": ["可用券"],
				"discountText": "立减20元"
			}]
		},
		"seckillVenue": {
			"startTime": 1594551600,
			"image": "https://du.hupucdn.com/FrwHKoaOvT9bp_QRPVylU5zuyhf5",
			"couponAmount": 88800,
			"limitAmount": 0,
			"activityUrl": "https://m.poizon.com/h5activity/seckillApp"
		}
	},
	"code": 200,
	"status": 200,
	"msg": "ok",
	"error": false
}
    
    
"""
