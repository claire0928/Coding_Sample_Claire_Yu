const Router = require('koa-router')
const read = require('node-readability')
const fs = require('fs')
const path = require('path')
const Models = require('../models')
const phantom = require('phantom')
const users = require('../users').items

const resolve = file => path.resolve(__dirname, file)
const router = new Router()

// define success template
let successState = {
	msg: 'success',
	code: 200,
	data: ''
}
// define failed template
let errorState = {
	msg: 'error',
	code: '',
	data: ''
}

const findUser = function(name, password){
	return users.find(function(item){
			return item.name === name && item.password === password;
	});
};

router.post('/api/login', async (ctx, next) => {
	let user = findUser(ctx.request.body.name, ctx.request.body.pwd);
	if(user){
		let session = ctx.session
		session.isLogin = true
		session.userName = ctx.request.body.name
		successState.msg = 'Successfully Login!'
		ctx.body = successState
	}else{
		errorState.msg = 'Incorrect account or password!'
		errorState.code = 400
		ctx.body = errorState
	}
})

router.get('/api/logout', async (ctx, next) => {
	ctx.session = null;
	let success = Object.assign({}, successState, {
		code: 200,
		msg: 'Sucessfully Sign Out!'
	})
	ctx.response.body = success
})


/**
 * note C、R、U、D
 */

router.get('/api/allNote', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		await Models.NoteList.find({user_name: ctx.session.userName}, (err, docs) => {
			if(err){
				ctx.throw(500)
				return
			}
			let success = Object.assign({}, successState, {
				data: docs.reverse()
			})
			ctx.response.body = success
			ctx.sessionHandler.regenerateId()
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

// generate note
router.post('/api/generateNote', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		const instance = await phantom.create()
		const page = await instance.createPage()
		const status = await page.open(ctx.request.body.link)
		const content = await page.property('content')
		await instance.exit()
		await new Promise((resolve, reject) => {
			read(content, (err, article, meta) => {
				if (err) {
					reject(err)
				} else {
					resolve(article)
				}
			})
		}).then((success) => {
			ctx.body = {
				title: success.title,
				content: success.content
			}
		})
	}else{
		let error = Object.assign({}, successState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

// add note
router.post('/api/addNote', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		ctx.request.body['user_name'] = ctx.session.userName
		await new Models.NoteList(ctx.request.body).save((err, docs) => {
			if(err){
				ctx.throw(500)
				return
			}
			successState.data = docs
			ctx.response.body = successState
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

// delete note
router.post('/api/deleteNote', async (ctx, next	) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		await Models.NoteList.remove({
			_id: ctx.request.body.id, 
			user_name: ctx.session.userName
		}, (err, docs) => {
			if(err){
				ctx.throw(500)
				return
			}
			successState.data = docs
			ctx.response.body = successState
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

// modify note
router.post('/api/modify', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		await new Promise((resolve, reject) => {
			Models.NoteList.update({
				_id: ctx.request.body._id, 
				user_name: ctx.session.userName
			}, ctx.request.body, (err, docs) => {
				if(err){
					reject(err)
				}else{
					resolve(docs)
				}
			})
		}).then((success) => {
			successState.data = ctx.request.body
			ctx.response.body = successState
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})


/**
 * label C、R、U、D
 */

router.post('/api/addTag', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		ctx.request.body['user_name'] = ctx.session.userName
		await new Models.TagList(ctx.request.body).save((err, docs) => {
			if(err){
				ctx.throw(500)
				return
			}
			successState.data = docs
			ctx.response.body = successState
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

router.post('/api/deleteTag', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		await Models.TagList.remove({
			_id: ctx.request.body.id,
			user_name: ctx.session.userName
		}, (err, docs) => {
			if(err){
				ctx.throw(500)
				return
			}
			successState.data = docs
			ctx.response.body = successState
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

router.get('/api/TagList', async (ctx, next) => {
	if ( ctx.session && ctx.session.isLogin && ctx.session.userName ) {
		await Models.TagList.find({user_name: ctx.session.userName}, (err, docs) => {
			if(err){
				ctx.throw(500)
				return
			}			
			let success = Object.assign({}, successState, {
				data: docs.reverse()
			})
			ctx.response.body = success
		})
	} else {
		let error = Object.assign({}, errorState, {
			code: 401,
			msg: 'Unauthorized'
		})
		ctx.body = error
	}
})

router.get('*', (ctx, next) => {
	const html = fs.readFileSync(resolve('../../../dist/' + 'index.html'), 'utf-8')
	ctx.body = html
})

module.exports = router