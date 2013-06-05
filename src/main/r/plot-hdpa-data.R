files <- matrix(c(
'nan-random-t5000-model-b10-k0.9-20130528-0703', 'coherence-20130531-0456.txt', 'eval-20130601-1256.txt',
'nan-random-t5000-model-b100-k0.9-20130528-0907', 'coherence-20130529-2149.txt', 'eval-20130530-0258.txt',
'nan-random-t5000-model-b1000-k0.9-20130528-0808', 'coherence-20130529-1044.txt', 'eval-20130529-1229.txt',
'nan-random-t5000-model-b50-k0.9-20130528-0907', 'coherence-20130529-2150.txt', 'eval-20130530-0551.txt',
'nan-random-t5000-model-b500-k0.5-20130528-0654', 'coherence-20130529-0657.txt', 'eval-20130529-0840.txt',
'nan-random-t5000-model-b500-k0.6-20130528-0656', 'coherence-20130529-0709.txt', 'eval-20130529-0901.txt',
'nan-random-t5000-model-b500-k0.7-20130528-0656', 'coherence-20130529-0755.txt', 'eval-20130529-0959.txt',
'nan-random-t5000-model-b500-k0.8-20130528-0703', 'coherence-20130529-0803.txt', 'eval-20130529-1013.txt',
'nan-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130530-1952.txt', 'eval-20130530-2209.txt',
'nan-random-t5000-model-b500-k1.0-20130528-0703', 'coherence-20130529-1559.txt', 'eval-20130529-1807.txt',
'nyt-random-t5000-model-b10-k0.9-20130528-0627', 'coherence-20130605-0831.txt', 'eval-20130601-1646.txt',
'nyt-random-t5000-model-b100-k0.9-20130528-0648', 'coherence-20130531-1706.txt', 'eval-20130601-0218.txt',
'nyt-random-t5000-model-b1000-k0.9-20130528-0653', 'coherence-20130530-2115.txt', 'eval-20130530-2324.txt',
'nyt-random-t5000-model-b50-k0.9-20130528-0640', 'coherence-20130530-1948.txt', 'eval-20130531-1256.txt',
'nyt-random-t5000-model-b500-k0.5-20130528-0558', 'coherence-20130529-2150.txt', 'eval-20130530-0022.txt',
'nyt-random-t5000-model-b500-k0.6-20130528-0559', 'coherence-20130529-2152.txt', 'eval-20130530-0030.txt',
'nyt-random-t5000-model-b500-k0.7-20130528-0611', 'coherence-20130530-0723.txt', 'eval-20130530-1001.txt',
'nyt-random-t5000-model-b500-k0.8-20130528-0614', 'coherence-20130530-0723.txt', 'eval-20130530-1025.txt',
'nyt-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130531-0455.txt', 'eval-20130531-0756.txt',
'nyt-random-t5000-model-b500-k1.0-20130528-0621', 'coherence-20130531-1702.txt', 'eval-20130531-2002.txt'
), 20, 3, byrow=T)

readEvalFile <- function(dir, file) {
	f <- readLines(paste('/Users/jonathan/Desktop/data', dir, file, sep='/'))
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

readCoherenceFile <- function(dir, file) {
	f <- readLines(paste('/Users/jonathan/Desktop/data', dir, file, sep='/'))
	f <- f[c(1, 3:302)]
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

createPdf <- function(dir, data.eval, data.coh) {
	pdf(paste('/Users/jonathan/Desktop/data', dir, "chart.pdf", sep='/'))
	par(mfrow=c(2,2), las=1, pin=c(2,2), oma=c(0, 0, 2 ,0))
	plot(data.eval$total.docs, data.eval$per.word, xaxt='n', ylab='likelihood', xlab='docs', type='l', main="Per-word log likelihood")
	maxx <- round(max(data.eval$total.docs), -5)
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
	
	title(main=dir, outer=T)
	dev.off()
}

makeCharts <- function(ff) {
	data.eval <- readEvalFile(ff[1], ff[3])
	data.coh <- readCoherenceFile(ff[1], ff[2])
	createPdf(ff[1], data.eval, data.coh)
}

apply(files, 1, makeCharts);