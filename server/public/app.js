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

    // Admin Info Modal logic
    const adminModal = document.getElementById('admin-modal');
    const adminModalContent = document.getElementById('admin-modal-content');
    const adminModalClose = document.getElementById('admin-modal-close');
    const adminModalBackdrop = document.getElementById('admin-modal-backdrop');
    const adminTriggers = document.querySelectorAll('.admin-trigger');

    const openAdminModal = () => {
        if (!adminModal || !adminModalContent) return;
        adminModal.classList.remove('opacity-0', 'pointer-events-none');
        adminModal.classList.add('opacity-100');
        adminModalContent.classList.remove('opacity-0', 'scale-95');
        adminModalContent.classList.add('opacity-100', 'scale-100');
    };

    const closeAdminModal = () => {
        if (!adminModal || !adminModalContent) return;
        adminModal.classList.remove('opacity-100');
        adminModal.classList.add('opacity-0', 'pointer-events-none');
        adminModalContent.classList.remove('opacity-100', 'scale-100');
        adminModalContent.classList.add('opacity-0', 'scale-95');
    };

    adminTriggers.forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            openAdminModal();
        });
    });

    if (adminModalClose) {
        adminModalClose.addEventListener('click', closeAdminModal);
    }

    if (adminModalBackdrop) {
        adminModalBackdrop.addEventListener('click', closeAdminModal);
    }
});
