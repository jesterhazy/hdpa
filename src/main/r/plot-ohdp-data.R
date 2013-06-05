basedir <- '/Users/jonathan/Documents/6 uathabasca/project/698 - implementation/wang comparison/dat/20130331 complete runs'

files <- matrix(c(
'corpus-nan-kappa-0.9-tau-1-batchsize-500', 13, 'coherence-20130605-1523.txt', 'test-log.dat',
'corpus-nyt-kappa-0.9-tau-1-batchsize-500', 14, 'coherence-20130605-1506.txt', 'test-log.dat'
), 2, 4, byrow=T)

readEvalFile <- function(ff) {
	f <- readLines(paste(basedir, ff[1], ff[4], sep='/'))
	f <- f[1:ff[2]]
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub(" +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

readCoherenceFile <- function(ff) {
	f <- readLines(paste(basedir, ff[1], ff[3], sep='/'))
	f <- f[c(1, 3:302)]
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

createPdf <- function(ff, data.eval, data.coh) {
	pdf(paste('/Users/jonathan/Desktop/', ff[1], "-chart.pdf", sep=''))
	par(mfrow=c(2,2), las=1, pin=c(2,2), oma=c(0, 0, 2 ,0))
	perword <- apply(as.matrix(data.eval$score), 1, function(x) x / data.eval$word.count[1])
	plot(data.eval$doc.count, perword, xaxt='n', ylab='likelihood', xlab='docs', type='l', main="Per-word log likelihood")
	maxx <- round(max(data.eval$doc.count), -5)
	aty = c(0, maxx/2, maxx)
	axis(1, at=aty, labels=formatC(aty, format="d"))
	box()
	
	plot(sort(data.coh$coherence, T), axes=F, ylab='coherence', xlab='topic', type='l', main='Topic Coherence')
	axis(2)
	box()
	
	plot(sort(data.coh$weight, T), axes=F, ylab='weight', xlab='topic', type='l', main='Topic Weights')
	axis(2)
	box()
	
	plot(data.coh$weight, data.coh$coherence, axes=F, ylab='coherence', xlab='weight', main='Coherence vs. Weight')
	abline(lm(data.coh$coherence~data.coh$weight), lty=2)
	axis(2)
	box()
	
	title(main=ff[1], outer=T)
	dev.off()
}

makeCharts <- function(ff) {
	data.eval <- readEvalFile(ff)
	data.coh <- readCoherenceFile(ff)
	createPdf(ff, data.eval, data.coh)
}

apply(files, 1, makeCharts);