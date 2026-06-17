// Download Portal Script - Aura Salon

document.addEventListener('DOMContentLoaded', () => {
    const versionText = document.getElementById('version-text');
    const downloadBtnHero = document.getElementById('btn-download-hero');
    const downloadBtnCta = document.getElementById('btn-download-cta');
    const releaseDetails = document.getElementById('release-details');
    const changelogText = document.getElementById('changelog-text');

    // Default fallback values if API fetch fails
    const fallbackApkUrl = 'mayank-gents.apk';
    const fallbackVersionName = '1.0.1';

    // Fetch the latest app version details from the backend
    fetch('../api/app-version')
        .then(response => {
            if (!response.ok) {
                throw new Error('Server returned invalid status');
            }
            return response.json();
        })
        .then(data => {
            // Update Version Text
            if (versionText) {
                versionText.textContent = `LATEST: v${data.versionName} (Build ${data.versionCode})`;
            }

            // Update Download Buttons Link
            const apkUrl = data.downloadUrl || fallbackApkUrl;
            if (downloadBtnHero) downloadBtnHero.href = apkUrl;
            if (downloadBtnCta) downloadBtnCta.href = apkUrl;

            // Update Changelog Details
            if (data.updateMessage && changelogText && releaseDetails) {
                changelogText.textContent = data.updateMessage;
                releaseDetails.classList.remove('hidden');
            }
        })
        .catch(err => {
            console.error('Failed to retrieve version details from server:', err);

            // Set fallback details so the user can still download the APK
            if (versionText) {
                versionText.textContent = `LATEST: v${fallbackVersionName} (Standalone)`;
            }
            if (downloadBtnHero) downloadBtnHero.href = fallbackApkUrl;
            if (downloadBtnCta) downloadBtnCta.href = fallbackApkUrl;
        });
});
