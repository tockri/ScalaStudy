var gulp = require('gulp');
var gutil = require('gulp-util');
var compass = require('gulp-compass');
var prefixer = require('gulp-autoprefixer');
var csscomb = require('gulp-csscomb');
var minifyCSS = require('gulp-minify-css');
var plumber = require('gulp-plumber');
var rename = require('gulp-rename');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var notify = require("gulp-notify");
var del = require('del');
var browser = require('browser-sync');
var runSequence = require('run-sequence');

//config
var root = 'public',
    config = {
        'path': {
            'image': root + '/images',
            'scss': root + '/scss',
            'css': root + '/stylesheets',
            'js': root + '/javascripts',
            'test_js': './test/javascripts',
            'compiled_css': root + '/stylesheets/compiled',
            'compiled_js': root + '/javascripts/compiled',
            'classes': 'target/scala-2.11/classes/**/*.class'
        }
    };

function optimizeJavaScript(stream) {
    if (gutil.env.type === 'build') {
        return stream
            .pipe(rename({suffix: '.min'}))
            .pipe(uglify())
            .pipe(gulp.dest(config.path.compiled_js));
    } else {
        return stream.pipe(gutil.noop());
    }
}

function optimizeCSS(stream) {
    if (gutil.env.type === 'build') {
        return stream
            .pipe(rename({suffix: '.min'}))
            .pipe(minifyCSS({
                'processImport': false,
                'restructuring': false
            }))
            .pipe(gulp.dest(config.path.compiled_css));
    } else {
        return stream.pipe(gutil.noop());
    }
}

gulp.task('compass', function () {
    return optimizeCSS(gulp.src(config.path.scss + '/*.scss')
        .pipe(gutil.env.type === 'build' ? gutil.noop() : plumber({errorHandler: notify.onError("Error: <%= error.message %>")}))
        .pipe(compass({
            bundle_exec: true,
            config_file: 'config.rb',
            sass: config.path.scss,
            css: config.path.compiled_css,
            image: config.path.image
        }))
        .pipe(prefixer('last 2 version'))
        .pipe(gutil.env.type === 'build' ? csscomb() : gutil.noop())
        .pipe(gutil.env.type === 'build' ? gutil.noop() : plumber.stop())
        .pipe(gulp.dest(config.path.compiled_css)));
});


gulp.task('js', function (callback) {
    runSequence('js-lib', 'js-app', callback);
});

gulp.task('js-lib', function () {
    return optimizeJavaScript(gulp.src([
        config.path.js + '/lib/jquery/jquery.js',
        config.path.js + '/lib/jquery/plugins/*.js',
        config.path.js + '/lib/*.js',
        config.path.js + '/app/lib/*.js',
        config.path.js + '/app/common/*.js'
    ])
        .pipe(gutil.env.type === 'build' ? gutil.noop() : plumber({errorHandler: notify.onError("Error: <%= error.message %>")}))
        .pipe(concat('lib.js'))
        .pipe(gulp.dest(config.path.compiled_js)));
});


gulp.task('js-app', function () {
    return optimizeJavaScript(gulp.src([
        config.path.js + '/app/*.js'
    ])
        .pipe(gutil.env.type === 'build' ? gutil.noop() : plumber({errorHandler: notify.onError("Error: <%= error.message %>")}))
        .pipe(gulp.dest(config.path.compiled_js)));
});

gulp.task('test', function () {
    return gulp.src('/test/javascripts')
        .pipe(karma({
            configFile: 'karma.conf.js',
            action: 'run'
        }));
});

gulp.task('autotest', function () {
    return gulp.watch([
        config.path.js + '/lib/**/*.js',
        config.path.js + '/app/**/*.js'
    ], ['test']);
});

gulp.task('clean', function () {
    return del([config.path.compiled_css, config.path.compiled_js], function () {
    });
});

gulp.task('compile', ['compass', 'js']);
gulp.task('watch', function () {
    gulp.watch(config.path.scss + '/**/*', ['compass']);
    gulp.watch([
        config.path.js + '/lib/**/*.js',
        config.path.js + '/app/**/*.js',
        config.path.js + '/resources/**/*.js'
    ], ['js']);
});

gulp.task('reload', function() {
    browser.reload({stream:true});
});

gulp.task('server', function () {
    browser.init({
        proxy: 'http://localhost:8485/integration/list',
        reloadDelay: 200
    });
    browser.watch(config.path.compiled_css + '/**/*.css', function(ev, file) {
        if (ev == 'change') {
            browser.reload(file);
        }
    });
    browser.watch(config.path.classes, function(ev, file) {
        if (ev == 'add') {
            browser.reload();
        }
    })
});

gulp.task('default', ['compile', 'watch']);
